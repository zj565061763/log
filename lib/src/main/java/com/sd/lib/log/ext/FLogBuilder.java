package com.sd.lib.log.ext;

import android.text.TextUtils;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class FLogBuilder implements ILogBuilder
{
    private ILogFormatter mFormatter;
    private final List<KeyValue> mList = new ArrayList<>();
    private boolean mHashPairView = true;

    @Override
    public ILogBuilder setFormatter(ILogFormatter formatter)
    {
        mFormatter = formatter;
        return this;
    }

    @Override
    public ILogBuilder setHashPairView(boolean hash)
    {
        mHashPairView = hash;
        return this;
    }

    private ILogFormatter getFormatter()
    {
        if (mFormatter == null)
            return InternalLogFormatter.DEFAULT;
        return mFormatter;
    }

    @Override
    public ILogBuilder add(Object content)
    {
        if (content == null)
            return this;

        if (content instanceof String)
        {
            if (TextUtils.isEmpty(content.toString()))
                return this;
        }

        mList.add(new KeyValue(null, content));
        return this;
    }

    @Override
    public ILogBuilder pair(String key, Object value)
    {
        if (TextUtils.isEmpty(key))
            return this;

        String stringValue = null;
        if (value == null)
        {
            stringValue = "null";
        } else
        {
            if (mHashPairView && (value instanceof View))
                stringValue = getInstanceHash(value);
            else
                stringValue = value.toString();
        }

        mList.add(new KeyValue(key, stringValue));
        return this;
    }

    @Override
    public ILogBuilder pairHash(String key, Object value)
    {
        return pair(key, getInstanceHash(value));
    }

    @Override
    public ILogBuilder pairStr(String key, Object value)
    {
        return pair(key, getInstanceString(value));
    }

    @Override
    public ILogBuilder instance(Object instance)
    {
        return pair("instance", getInstanceHash(instance));
    }

    @Override
    public ILogBuilder instanceStr(Object instance)
    {
        final String stringValue = instance == null ? "null" : instance.toString();
        return pair("instanceStr", stringValue);
    }

    @Override
    public ILogBuilder uuid(String uuid)
    {
        return pair("uuid", uuid);
    }

    @Override
    public ILogBuilder nextLine()
    {
        return add("\r\n");
    }

    @Override
    public ILogBuilder clear()
    {
        mList.clear();
        return this;
    }

    @Override
    public String build()
    {
        if (mList.isEmpty())
            return "";

        final StringBuilder builder = new StringBuilder();

        int index = 0;
        for (KeyValue item : mList)
        {
            if (index == 0)
                builder.append(" ");
            else
                builder.append(getFormatter().getSeparatorBetweenPart());

            if (TextUtils.isEmpty(item.key))
            {
                builder.append(item.value);
            } else
            {
                builder.append(item.key).append(getFormatter().getSeparatorForKeyValue()).append(item.value);
            }

            index++;
        }

        return builder.toString();
    }

    @Override
    public String toString()
    {
        return build();
    }

    private static String getInstanceHash(Object object)
    {
        return object == null ? null : object.getClass().getName() + "@" + Integer.toHexString(object.hashCode());
    }

    private static String getInstanceString(Object object)
    {
        return object == null ? null : object.toString();
    }

    private final class KeyValue
    {
        public final String key;
        public final Object value;

        public KeyValue(String key, Object value)
        {
            this.key = key;
            this.value = value;
        }
    }

    private static final class InternalLogFormatter implements ILogFormatter
    {
        public static final InternalLogFormatter DEFAULT = new InternalLogFormatter();

        @Override
        public String getSeparatorForKeyValue()
        {
            return ":";
        }

        @Override
        public String getSeparatorBetweenPart()
        {
            return "|";
        }
    }
}
