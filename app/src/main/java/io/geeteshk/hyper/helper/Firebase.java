package io.geeteshk.hyper.helper;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;

public class Firebase {

    private static final String GS_BUCKET = "gs://hyper-a0ee4.appspot.com";
    private static final int ONE_MEGABYTE = 1024 * 1024;

    private static StorageReference getStorageRef(FirebaseStorage storage) {
        return storage.getReferenceFromUrl(GS_BUCKET);
    }

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

    private static void uploadFile(FirebaseAuth auth, FirebaseStorage storage, File file) {
        Uri fileUri = Uri.fromFile(file);
        StorageReference fileRef = getStorageRef(storage).child(auth.getCurrentUser().getUid() + File.separator + file.getPath().substring(file.getPath().indexOf("Hyper/") + 6, file.getPath().length()));
        UploadTask uploadTask = fileRef.putFile(fileUri);
    }

    private static String addProjectToJson(String project, String json) throws JSONException {
        JSONArray array;
        array = json != null ? new JSONArray(json) : new JSONArray();
        array.put(project);
        return array.toString();
    }

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

    private static void createStructureJson(File[] files, String project, JSONArray array) {
        for (File file : files) {
            if (file.isFile()) {
                array.put(file.getPath().substring(file.getPath().indexOf(project) + project.length() + 1, file.getPath().length()));
            } else {
                createStructureJson(file.listFiles(), project, array);
            }
        }
    }

    private static void addStructure(FirebaseAuth auth, FirebaseStorage storage, String project) {
        StorageReference structureRef = getStorageRef(storage).child(auth.getCurrentUser().getUid() + File.separator + project + File.separator + ".structure");
        File[] files = new File(Constants.HYPER_ROOT + File.separator + project).listFiles();
        JSONArray array = new JSONArray();
        createStructureJson(files, project, array);
        structureRef.putBytes(array.toString().getBytes());
    }

    private static void addProject(FirebaseAuth auth, FirebaseStorage storage, final String project) {
        final StorageReference projectRef = getStorageRef(storage).child(auth.getCurrentUser().getUid() + File.separator + ".projects");
        projectRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                try {
                    String toUpload = addProjectToJson(project, new String(bytes));
                    projectRef.putBytes(toUpload.getBytes());
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
                        projectRef.putBytes(toUpload.getBytes());
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    public static void deleteProjectFiles(final FirebaseAuth auth, final FirebaseStorage storage, final String project) {
        StorageReference structureRef = getStorageRef(storage).child(auth.getCurrentUser().getUid() + File.separator + project + File.separator + ".structure");
        structureRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
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

    public static void updateProject(final FirebaseAuth auth, final FirebaseStorage storage, final String project, final boolean newProject) {
        StorageReference structureRef = getStorageRef(storage).child(auth.getCurrentUser().getUid() + File.separator + project + File.separator + ".structure");
        structureRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
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

    public static void removeProject(FirebaseAuth auth, FirebaseStorage storage, final String project) {
        final StorageReference projectRef = getStorageRef(storage).child(auth.getCurrentUser().getUid() + File.separator + ".projects");
        projectRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                try {
                    String toUpload = removeProjectFromJson(project, new String(bytes));
                    projectRef.putBytes(toUpload.getBytes());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void syncProjects(final FirebaseAuth auth, final FirebaseStorage storage) {
        StorageReference projectRef = getStorageRef(storage).child(auth.getCurrentUser().getUid() + File.separator + ".projects");
        projectRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
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

    private static void downloadProject(final FirebaseAuth auth, final FirebaseStorage storage, final String project) {
        StorageReference structureRef = getStorageRef(storage).child(auth.getCurrentUser().getUid() + File.separator + project + File.separator + ".structure");
        final File projectFile = new File(Constants.HYPER_ROOT + File.separator + project);
        structureRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                try {
                    JSONArray array = new JSONArray(new String(bytes));
                    for (int i = 0; i < array.length(); i++) {
                        StorageReference fileRef = getStorageRef(storage).child(auth.getCurrentUser().getUid() + File.separator + project + File.separator + array.getString(i));
                        File localFile = new File(projectFile, array.getString(i));
                        localFile.getParentFile().mkdirs();
                        localFile.createNewFile();
                        fileRef.getFile(localFile);
                    }

                    File fontsDir = new File(projectFile, "fonts");
                    if (!fontsDir.exists()) fontsDir.mkdir();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
