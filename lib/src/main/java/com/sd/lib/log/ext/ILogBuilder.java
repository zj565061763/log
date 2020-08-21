package com.sd.lib.log.ext;

public interface ILogBuilder
{
    ILogBuilder setFormatter(ILogFormatter formatter);

    ILogBuilder add(Object content);

    ILogBuilder pair(String key, Object value);

    ILogBuilder instance(Object instance);

    ILogBuilder uuid(String uuid);

    ILogBuilder clear();

    String build();

    interface ILogFormatter
    {
        String getSeparatorForKeyValue();

        String getSeparatorBetweenPart();
    }
}
