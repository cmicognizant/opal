package org.obiba.opal.core.support;

import java.util.Date;

/**
 Impl of MessageListener that just System.out.println all the messages
 */
public class SystemOutMessageLogger implements MessageLogger {

    @Override
    public void debug(String msgFormat, Object... args) {
        out("DEBUG", msgFormat, args);
    }

    @Override
    public void info(String msgFormat, Object... args) {
        out("INFO", msgFormat, args);
    }

    @Override
    public void warn(String msgFormat, Object... args) {
        out("WARN", msgFormat, args);
    }

    @Override
    public void error(String msgFormat, Object... args) {
        out("ERROR", msgFormat, args);
    }

    private void out(String type, String msgFormat, Object... args) {
        String msg = String.format(msgFormat, args);
        String fullMsg = String.format(MESSAGE_FORMAT, new Date(), type, msg);
        System.out.println(fullMsg);
    }

}
