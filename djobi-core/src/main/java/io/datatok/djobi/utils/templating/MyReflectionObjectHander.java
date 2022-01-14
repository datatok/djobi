package io.datatok.djobi.utils.templating;

import com.github.mustachejava.Binding;
import com.github.mustachejava.Code;
import com.github.mustachejava.TemplateContext;
import com.github.mustachejava.reflect.ReflectionObjectHandler;

public class MyReflectionObjectHander extends ReflectionObjectHandler {

    @Override
    public Binding createBinding(String name, TemplateContext tc, Code code) {
        return new MyGuardedBinding(this, name, tc, code);
    }

}
