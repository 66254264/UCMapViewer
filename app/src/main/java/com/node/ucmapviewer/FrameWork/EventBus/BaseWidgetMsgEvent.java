package com.node.ucmapviewer.FrameWork.EventBus;

/**
 * Widget系统消息事件
 */
public class BaseWidgetMsgEvent {
    private String message;

    public BaseWidgetMsgEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
