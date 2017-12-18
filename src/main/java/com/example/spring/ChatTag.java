package com.example.spring;

import com.vaadin.ui.Component;
import com.vaadin.ui.Tag;
import com.vaadin.ui.common.ClientDelegate;
import com.vaadin.ui.common.HtmlImport;

import java.io.Serializable;

@Tag("chat-tag")
@HtmlImport("chattag.html")
public class ChatTag extends Component {
    @ClientDelegate
    public void chatInput(String line) {
        MainView.sendLine(line);
    }
    public void chatOutput(String line) {
        getElement().callFunction("chatOutput", line);
    }
}
