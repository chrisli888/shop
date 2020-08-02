package lz.demo.shop.web.action;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Data
@Slf4j
@RequiredArgsConstructor
public class Action<T> {
    @NonNull
    private Object object;
    @NonNull
    private Method method;

    private boolean injectionFullhttprequest;

    public T call(Object ...args) {
        try {
            return (T) method.invoke(object,args);
        }catch (IllegalAccessException | InvocationTargetException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
