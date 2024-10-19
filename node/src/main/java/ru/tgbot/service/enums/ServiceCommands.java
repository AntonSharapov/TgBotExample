package ru.tgbot.service.enums;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public enum ServiceCommands {
    HELP("/help"),
    REGISTRATION("/registration"),
    CANCEL("/cancel"),
    START("/start");
    private final String cmd;
    ServiceCommands(String cmd) {
        this.cmd = cmd;
    }

    @Override
    public String toString() {
        return cmd;
    }
    public boolean equals(String cmd) {
        return this.toString().equals(cmd);
    }

    public  static ServiceCommands fromValue(String v) {
        for(ServiceCommands c: ServiceCommands.values()){
            if(c.cmd.equals(v)) {
                return c;
            }
        }
        return null;
    }

}
