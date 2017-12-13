package com.example.spring;

import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.Transport;
import com.vaadin.ui.Push;
import com.vaadin.ui.UI;

@Push(transport = Transport.LONG_POLLING)
public class KafkaUI extends UI {
    @Override
    protected void init(VaadinRequest request) {
        super.init(request);
    }
}
