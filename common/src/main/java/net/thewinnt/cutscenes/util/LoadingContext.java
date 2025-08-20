package net.thewinnt.cutscenes.util;

import net.minecraft.resources.ResourceLocation;
import net.thewinnt.cutscenes.easing.Easing;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Holds all the necessary data for loading a cutscene.
 */
public final class LoadingContext {
    public final @Nullable LoadResolver<Easing> easings;
    private final StackElement root = new StackElement("root", null);
    private int dataVersion;
    private StackElement last = root;

    public LoadingContext(@Nullable LoadResolver<Easing> easings) {
        this.easings = easings;
    }

    public void setDataVersion(int dataVersion) {
        this.dataVersion = dataVersion;
    }

    public int getDataVersion() {
        return dataVersion;
    }

    public void pushElement(String name) {
        this.last = this.last.addChild(name);
    }

    public void popElement() {
        if (this.last.parent == null) {
            throw new IllegalStateException("Trying to pop the root element!");
        }
        this.last = this.last.parent;
    }

    public void reportError(String error) {
        this.last.reportError(error);
    }

    public List<String> getErrors() {
        if (last != root && last != null) {
            root.reportError("LoadingContext not popped all the way through! Remainder: " + last.fullName());
        }
        return root.createReport(1);
    }

    public void clear() {
        root.clear();
    }

    public <T> T wrapLoading(String name, Supplier<T> loader) {
        return this.wrapLoading(name, loader, null);
    }

    public <T> T wrapLoading(String name, Supplier<T> loader, T fallback) {
        this.pushElement(name);
        try {
            return loader.get();
        } catch (Exception e) {
            this.reportError("Uncaught exception: " + e);
            return fallback;
        } finally {
            this.popElement();
        }
    }

    private static class StackElement {
        private final List<StackElement> children = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();
        private final String name;
        private final StackElement parent;

        public StackElement(String name, StackElement parent) {
            this.name = name;
            this.parent = parent;
        }

        private StackElement addChild(String name) {
            StackElement output = new StackElement(name, this);
            this.children.add(output);
            return output;
        }

        private void reportError(String error) {
            this.errors.add(error);
        }

        public void clear() {
            this.children.clear();
            this.errors.clear();
        }

        public List<String> createReport(int depth) {
            List<String> output = new ArrayList<>();
            for (StackElement i : this.children) {
                List<String> local = new ArrayList<>();
                local.add("-".repeat(depth) + " Errors in element " + i.name + ":");
                local.addAll(i.createReport(depth + 1));
                for (String j : i.errors) {
                    local.add("-".repeat(depth + 1) + " " + j);
                }
                if (local.size() > 1) { // any of the children had errors, or the element has errors
                    output.addAll(local);
                }
            }
            return output;
        }

        public String fullName() {
            if (parent == null) return name;
            return parent.fullName() + "/" + name;
        }
    }
}
