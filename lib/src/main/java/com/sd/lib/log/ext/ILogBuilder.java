package com.sd.lib.log.ext;

public interface ILogBuilder
{
    ILogBuilder add(Object content);

    ILogBuilder add(String key, Object value);

    ILogBuilder uuid(String uuid);

    ILogBuilder clear();

    String build();

    interface ILogFormatter
    {
        String getSeparatorForKeyValue();

        String getSeparatorBetweenPart();
    }
}
