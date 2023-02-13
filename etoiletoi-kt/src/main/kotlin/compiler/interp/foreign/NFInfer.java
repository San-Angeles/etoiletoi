package compiler.interp.foreign;

import java.lang.foreign.MemoryLayout;

public class NFInfer {

    private final String name;
    private final MemoryLayout resLayout;

    public NFInfer(String name, MemoryLayout resLayout) {
        this.name = name;
        this.resLayout = resLayout;
    }

    public NFInfer(String name) {
        this(name, null);
    }

    public Object invoke(Object... args) throws Throwable {
        var argLayouts = new MemoryLayout[args.length];
        for (int index = 0; index < argLayouts.length; index++) {
            argLayouts[index] = by(args[index]);
            if (args[index] instanceof String s)
                args[index] = CLib.string(s);
        }
        var handle = CLib.downcall(resLayout, name, argLayouts);
        return handle.invokeWithArguments(args);
    }

    public Object invokeOrCalm(Object... args) {
        try {
            return invoke(args);
        } catch (Throwable e) {
            return null;
        }
    }

    public static MemoryLayout by(Object value) {
        return switch (value) {
            case Byte i -> CLib.Byte;
            case Character i -> CLib.Char;
            case Short i -> CLib.Short;
            case Integer i -> CLib.Int;
            case Long i -> CLib.Long;
            case Float i -> CLib.Float;
            case Double i -> CLib.Double;
            default -> CLib.Addr;
        };
    }
}
