package com.sipaman.maintenance;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ActivityLogHelper {

    public static void log(String type, String taskId, String taskTitle, String message) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user == null ? "-" : user.getUid();
        String name = "User";

        if (user != null) {
            if (user.getDisplayName() != null && !user.getDisplayName().trim().isEmpty()) {
                name = user.getDisplayName();
            } else if (user.getEmail() != null) {
                name = user.getEmail();
            }
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("activity_logs").push();
        ActivityLog log = new ActivityLog(
                ref.getKey(),
                uid,
                name,
                type,
                System.currentTimeMillis(),
                taskId,
                taskTitle,
                message
        );
        ref.setValue(log);
    }
}
