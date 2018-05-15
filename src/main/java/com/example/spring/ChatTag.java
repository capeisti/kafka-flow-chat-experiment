package com.example.spring;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;

@Tag("chat-tag")
@HtmlImport("chattag.html")
public class ChatTag extends Component {
    public ChatTag() {
        getElement().setProperty("nickname", "Loldier");
    }

    public String getNickname() {
        return getElement().getProperty("nickname");
    }

    @ClientCallable
    public void chatInput(String line) {
        MainView.sendLine(line, getNickname());
    }

    public void chatOutput(String line, String nickname) {
        getElement().callFunction("chatOutput", line, nickname);
    }
}
