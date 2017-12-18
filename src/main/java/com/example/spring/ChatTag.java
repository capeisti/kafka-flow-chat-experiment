package com.example.spring;

import com.vaadin.ui.Component;
import com.vaadin.ui.Tag;
import com.vaadin.ui.common.ClientDelegate;
import com.vaadin.ui.common.HtmlImport;

import java.io.Serializable;

@Tag("chat-tag")
@HtmlImport("chattag.html")
public class ChatTag extends Component {
    public ChatTag() {
        getElement().setProperty("nickname", "Loldier");
    }

    public String getNickname() {
        return getElement().getProperty("nickname");
    }

    @ClientDelegate
    public void chatInput(String line) {
        MainView.sendLine(line, getNickname());
    }
    public void chatOutput(String line, String nickname) {
        getElement().callFunction("chatOutput", line, nickname);
    }
}
