package io.geeteshk.hyper.helper;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;

/**
 * Helper class to handle Firebase functions
 */
public class Firebase {

    /**
     * Getter for root storage reference
     *
     * @param storage used to get bucket reference
     * @return the root reference
     */
    private static StorageReference getStorageRef(FirebaseStorage storage) {
        return storage.getReferenceFromUrl(Constants.GS_BUCKET);
    }

    /**
     * Method to upload local project
     *
     * @param auth auth to get user id
     * @param storage storage to well storage
     * @param project project to upload
     * @param repeat whether the instance is a recursive one
     * @param newProject whether this upload is happening after creating a project
     */
    public static void uploadProject(FirebaseAuth auth, FirebaseStorage storage, String project, boolean repeat, boolean newProject) {
        if (!repeat) {
            if (newProject) {
                addProject(auth, storage, project);
            }

            addStructure(auth, storage, project);
        }

        File projectFile = new File(Constants.HYPER_ROOT + File.separator + project);
        File[] projectFiles = projectFile.listFiles();
        for (File file : projectFiles) {
            if (file.isDirectory()) {
                uploadProject(auth, storage, project + File.separator +  file.getName(), true, newProject);
            } else {
                uploadFile(auth, storage, file);
            }
        }
    }

    /**
     * Method to upload a file to a project
     *
     * @param auth auth to get user id
     * @param storage storage to storage
     * @param file file to upload
     */
    private static void uploadFile(FirebaseAuth auth, FirebaseStorage storage, File file) {
        Uri fileUri = Uri.fromFile(file);
        StorageReference fileRef = null;
        if (auth.getCurrentUser() != null) {
            fileRef = getStorageRef(storage).child(auth.getCurrentUser().getUid() + File.separator + file.getPath().substring(file.getPath().indexOf("Hyper/") + 6, file.getPath().length()));
        }

        if (fileRef != null) {
            fileRef.putFile(fileUri);
        }
    }

    /**
     * Adds the project to projects json
     *
     * @param project project to add
     * @param json json to add to
     * @return updated json
     * @throws JSONException something wrong while parsing json
     */
    private static String addProjectToJson(String project, String json) throws JSONException {
        JSONArray array;
        array = json != null ? new JSONArray(json) : new JSONArray();
        array.put(project);
        return array.toString();
    }

    /**
     * Remove a project from projects json
     *
     * @param project project to remove
     * @param json json to remove from
     * @return updated json
     * @throws JSONException something wrong while parsing json
     */
    private static String removeProjectFromJson(String project, String json) throws JSONException {
        JSONArray array = new JSONArray(json);
        JSONArray newArray = new JSONArray();
        for (int i = 0; i < array.length(); i++) {
            if (!array.getString(i).equals(project)) {
                newArray.put(array.getString(i));
            }
        }

        return newArray.toString();
    }

    /**
     * Create json for project file structure
     * This is required for users to be able to delete projects
     * due to limited Firebase API
     *
     * @param files to generate structure
     * @param project project to generate for
     * @param array array to add paths to
     */
    private static void createStructureJson(File[] files, String project, JSONArray array) {
        for (File file : files) {
            if (file.isFile()) {
                array.put(file.getPath().substring(file.getPath().indexOf(project) + project.length() + 1, file.getPath().length()));
            } else {
                createStructureJson(file.listFiles(), project, array);
            }
        }
    }

    /**
     * Upload generated structure
     *
     * @param auth auth to get user id
     * @param storage storage to storage
     * @param project project to add to
     */
    private static void addStructure(FirebaseAuth auth, FirebaseStorage storage, String project) {
        StorageReference structureRef = null;
        if (auth.getCurrentUser() != null) {
            structureRef = getStorageRef(storage).child(auth.getCurrentUser().getUid() + File.separator + project + File.separator + ".structure");
        }

        File[] files = new File(Constants.HYPER_ROOT + File.separator + project).listFiles();
        JSONArray array = new JSONArray();
        createStructureJson(files, project, array);

        if (structureRef != null) {
            structureRef.putBytes(array.toString().getBytes());
        }
    }

    /**
     * Method to add project to cloud
     *
     * @param auth auth to get user id
     * @param storage storage to storage
     * @param project to add
     */
    private static void addProject(FirebaseAuth auth, FirebaseStorage storage, final String project) {
        StorageReference projectRef = null;
        if (auth.getCurrentUser() != null) {
            projectRef = getStorageRef(storage).child(auth.getCurrentUser().getUid() + File.separator + ".projects");
        }

        final StorageReference finalProjectRef = projectRef;
        if (finalProjectRef != null) {
            finalProjectRef.getBytes(Constants.ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    try {
                        String toUpload = addProjectToJson(project, new String(bytes));
                        finalProjectRef.putBytes(toUpload.getBytes());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    if (e instanceof StorageException) {
                        try {
                            String toUpload = addProjectToJson(project, null);
                            finalProjectRef.putBytes(toUpload.getBytes());
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    /**
     * Delete files of project
     *
     * @param auth auth to get user id
     * @param storage storage to storage
     * @param project project to delete
     */
    public static void deleteProjectFiles(final FirebaseAuth auth, final FirebaseStorage storage, final String project) {
        StorageReference structureRef = null;
        if (auth.getCurrentUser() != null) {
            structureRef = getStorageRef(storage).child(auth.getCurrentUser().getUid() + File.separator + project + File.separator + ".structure");
        }

        if (structureRef != null) {
            structureRef.getBytes(Constants.ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    try {
                        JSONArray array = new JSONArray(new String(bytes));
                        for (int i = 0; i < array.length(); i++) {
                            StorageReference fileRef = getStorageRef(storage).child(auth.getCurrentUser().getUid() + File.separator + project + File.separator + array.getString(i));
                            fileRef.delete();
                        }

                        StorageReference fileRef = getStorageRef(storage).child(auth.getCurrentUser().getUid() + File.separator + project + File.separator + ".structure");
                        fileRef.delete();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * Method to update a project
     *
     * @param auth auth to get user id
     * @param storage storage to storage
     * @param project project to update
     * @param newProject whether project has just been created
     */
    public static void updateProject(final FirebaseAuth auth, final FirebaseStorage storage, final String project, final boolean newProject) {
        StorageReference structureRef = null;
        if (auth.getCurrentUser() != null) {
            structureRef = getStorageRef(storage).child(auth.getCurrentUser().getUid() + File.separator + project + File.separator + ".structure");
        }

        if (structureRef != null) {
            structureRef.getBytes(Constants.ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    try {
                        JSONArray array = new JSONArray(new String(bytes));
                        for (int i = 0; i < array.length(); i++) {
                            StorageReference fileRef = getStorageRef(storage).child(auth.getCurrentUser().getUid() + File.separator + project + File.separator + array.getString(i));
                            fileRef.delete();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    uploadProject(auth, storage, project, false, newProject);
                }
            });
        }
    }

    /**
     * Method to fully remove project
     *
     * @param auth auth to get user id
     * @param storage storage to storage
     * @param project project to remove
     */
    public static void removeProject(FirebaseAuth auth, FirebaseStorage storage, final String project) {
        StorageReference projectRef = null;
        if (auth.getCurrentUser() != null) {
            projectRef = getStorageRef(storage).child(auth.getCurrentUser().getUid() + File.separator + ".projects");
        }

        final StorageReference finalProjectRef = projectRef;
        if (finalProjectRef != null) {
            finalProjectRef.getBytes(Constants.ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    try {
                        String toUpload = removeProjectFromJson(project, new String(bytes));
                        finalProjectRef.putBytes(toUpload.getBytes());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * Syncs all of users projects from cloud storage
     *
     * @param auth auth to get user id
     * @param storage storage to storage
     */
    public static void syncProjects(final FirebaseAuth auth, final FirebaseStorage storage) {
        StorageReference projectRef = null;
        if (auth.getCurrentUser() != null) {
            projectRef = getStorageRef(storage).child(auth.getCurrentUser().getUid() + File.separator + ".projects");
        }

        if (projectRef != null) {
            projectRef.getBytes(Constants.ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    try {
                        JSONArray array = new JSONArray(new String(bytes));
                        for (int i = 0; i < array.length(); i++) {
                            downloadProject(auth, storage, array.getString(i));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * Method to fully remove a user from Firebase
     *
     * @param auth auth to get user
     * @param storage storage to remove user data
     */
    public static void removeUser(final FirebaseAuth auth, final FirebaseStorage storage) {
        StorageReference projectRef = null;
        if (auth.getCurrentUser() != null) {
            projectRef = getStorageRef(storage).child(auth.getCurrentUser().getUid() + File.separator + ".projects");
        }

        if (projectRef != null) {
            projectRef.getBytes(Constants.ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    try {
                        JSONArray array = new JSONArray(new String(bytes));
                        for (int i = 0; i < array.length(); i++) {
                            removeProject(auth, storage, array.getString(i));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        StorageReference userRef = null;
        if (auth.getCurrentUser() != null) {
            userRef = getStorageRef(storage).child(auth.getCurrentUser().getUid());
        }

        if (userRef != null) {
            userRef.delete();
        }
    }

    /**
     * Method to download a project from cloud
     *
     * @param auth auth to get user id
     * @param storage storage to storage
     * @param project project to download
     */
    private static void downloadProject(final FirebaseAuth auth, final FirebaseStorage storage, final String project) {
        StorageReference structureRef = null;
        if (auth.getCurrentUser() != null) {
            structureRef = getStorageRef(storage).child(auth.getCurrentUser().getUid() + File.separator + project + File.separator + ".structure");
        }

        final File projectFile = new File(Constants.HYPER_ROOT + File.separator + project);
        if (structureRef != null) {
            structureRef.getBytes(Constants.ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    try {
                        JSONArray array = new JSONArray(new String(bytes));
                        for (int i = 0; i < array.length(); i++) {
                            StorageReference fileRef = getStorageRef(storage).child(auth.getCurrentUser().getUid() + File.separator + project + File.separator + array.getString(i));
                            File localFile = new File(projectFile, array.getString(i));
                            if (localFile.getParentFile().mkdirs() || localFile.createNewFile()) {
                                fileRef.getFile(localFile);
                            }
                        }

                        File fontsDir = new File(projectFile, "fonts");
                        if (!fontsDir.exists()) {
                            if (!fontsDir.mkdir()) {
                                throw new IOException();
                            }
                        }
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
