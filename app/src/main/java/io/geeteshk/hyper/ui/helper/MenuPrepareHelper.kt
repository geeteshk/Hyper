package io.geeteshk.hyper.ui.helper

import android.view.Menu
import io.geeteshk.hyper.R

class MenuPrepareHelper {

    companion object {

        fun prepare(menu: Menu, vararg params: Boolean): Boolean {
            with (menu) {
                findItem(R.id.action_git_add).isEnabled = params[1] // is the file a git repo?
                findItem(R.id.action_git_log).isEnabled = params[1]
                findItem(R.id.action_git_diff).isEnabled = params[1]
                findItem(R.id.action_git_status).isEnabled = params[1]
                findItem(R.id.action_git_branch).isEnabled = params[1]
                findItem(R.id.action_git_remote).isEnabled = params[1]

                if (params[1]) {
                    findItem(R.id.action_git_commit).isEnabled = params[2] // is it possible to make a commit?
                    findItem(R.id.action_git_push).isEnabled = params[3] // does the repo have any remotes?
                    findItem(R.id.action_git_pull).isEnabled = params[3]
                    findItem(R.id.action_git_branch_checkout).isEnabled = params[4] // can the repo checkout?
                }
            }

            return true
        }
    }
}