package com.contrastsecurity.config;

import com.intellij.util.messages.Topic;

public interface ChangeActionNotifier {
    Topic<ChangeActionNotifier> CHANGE_ACTION_TOPIC = Topic.create("Organization change", ChangeActionNotifier.class);

    void beforeAction();

    void afterAction();
}
