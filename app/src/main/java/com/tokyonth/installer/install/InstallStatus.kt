package com.tokyonth.installer.install

enum class InstallStatus constructor(val code: Int) {

    SUCCESS(0),
    FAILURE(-1);

    companion object {
        operator fun invoke(code: Int): InstallStatus {
            return if (code == SUCCESS.code) {
                SUCCESS
            } else {
                FAILURE
            }
        }
    }

}