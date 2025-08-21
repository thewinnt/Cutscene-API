package net.thewinnt.cutscenes.util;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.thewinnt.cutscenes.easing.Easing;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Holds all the necessary data for loading a cutscene and provides a place to report errors to.
 */
public final class LoadingContext {
    /** The resolver for easings. Available when loading easing macros. */
    public final @Nullable LoadResolver<Easing> easings;
    /** The object holding the errors for all the elements in the cutscene. */
    private final StackElement root = new StackElement("root", null);
    /** The data version for the current cutscene. */
    private int dataVersion;
    /** The current element being reported. */
    private StackElement last = root;

    /**
     * Constructs a new {@link LoadingContext}.
     * @param easings the loader for easing macros.
     */
    public LoadingContext(@Nullable LoadResolver<Easing> easings) {
        this.easings = easings;
    }

    /**
     * Sets the data version of the current cutscene being loaded.
     * @param dataVersion the data version.
     */
    public void setDataVersion(int dataVersion) {
        this.dataVersion = dataVersion;
    }

    /**
     * Returns the data version of the cutscene being loaded.
     */
    public int getDataVersion() {
        return dataVersion;
    }

    /**
     * Starts collecting errors of a new element, being a child element of the current one.
     * @param name the name of the new element.
     */
    public void pushElement(String name) {
        this.last = this.last.addChild(name);
    }

    /**
     * Ends collecting errors for the current element, returning to its parent.
     */
    public void popElement() {
        if (this.last.parent == null) {
            throw new IllegalStateException("Trying to pop the root element!");
        }
        this.last = this.last.parent;
    }

    /**
     * Reports an error for the current element. Any errors found while loading a cutscene will
     * prevent it from being used.
     * @param error The error message to report
     */
    public void reportError(String error) {
        this.last.reportError(error);
    }

    /**
     * Gets a list of strings, containing all the errors found during loading so far.
     * @return a list of lines for each error found as well as the names of the elements with errors
     */
    public List<String> getErrors() {
        if (last != root && last != null) {
            root.reportError("LoadingContext not popped all the way through! Remainder: " + last.fullName());
        }
        return root.createReport(1);
    }

    /**
     * Prepares the loader for a next cutscene, clearing all the found errors and setting the current
     * element to the root one.
     */
    public void clear() {
        root.clear();
        last = root;
    }

    /**
     * Loads an element with the specified name, catching and reporting any exceptions that may occur.
     * @param name the name of the element
     * @param loader a function that loads the element
     * @return the result of {@code loader}, or {@code null} if an exception is thrown by {@code loader}
     * @param <T> the output type of {@code loader}
     */
    public <T> T wrapLoading(String name, Supplier<T> loader) {
        return this.wrapLoading(name, loader, null);
    }

    /**
     * Loads an element with the specified name, catching and reporting any exceptions that may occur.
     * Returns a fallback element in case of an error.
     * @param name the name of the element
     * @param loader a function that loads the element
     * @param fallback the object to be returned if an exception occurs. Note that the error is still reported, so
     *                 this should only be used in cases where a {@code null} value would cause an exception itself
     *                 (e.g. a {@link Double})
     * @return the result of {@code loader}
     * @param <T> the output type of {@code loader}
     */
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

    /**
     * Loads an element with the specified name, catching and reporting any exceptions that may occur,
     * returning {@code null} if any errors are reported in the process
     * @param name the name of the element
     * @param loader a function that loads the element
     * @return the result of {@code loader}, or {@code null} if an exception is thrown or an error is
     *         reported by {@code loader}
     * @param <T> the output type of {@code loader}
     */
    public <T> T wrapStrict(String name, Supplier<T> loader) {
        return this.wrapStrict(name, loader, null);
    }

    /**
     * Loads an element with the specified name, catching and reporting any exceptions that may occur,
     * returning {@code fallback} if any errors are reported in the process.
     * @param name the name of the element
     * @param loader a function that loads the element
     * @return the result of {@code loader}, or {@code fallback} if an exception is thrown or an error is
     *         reported by {@code loader}
     * @param <T> the output type of {@code loader}
     */
    public <T> T wrapStrict(String name, Supplier<T> loader, T fallback) {
        this.pushElement(name);
        StackElement element = last;
        try {
            T output = loader.get();
            if (element.createReport(0).isEmpty()) {
                return output;
            }
            return null;
        } catch (Exception e) {
            this.reportError("Uncaught exception: " + e);
            return fallback;
        } finally {
            this.popElement();
        }
    }

    /**
     * Represent an element with children and errors that may have occured while loading.
     */
    private static class StackElement {
        /** The list of child elements. */
        private final List<StackElement> children = new ArrayList<>();
        /** The list of errors on this element. */
        private final List<String> errors = new ArrayList<>();
        /** The name of this element. */
        private final String name;
        /** The element holding this one. */
        private final StackElement parent;

        public StackElement(String name, StackElement parent) {
            this.name = name;
            this.parent = parent;
        }

        /**
         * Adds and returns a child element.
         * @param name the name of the new element.
         * @return the newly created child element.
         */
        private StackElement addChild(String name) {
            StackElement output = new StackElement(name, this);
            this.children.add(output);
            return output;
        }

        /**
         * Reports an error for this element.
         * @param error the error message
         */
        private void reportError(String error) {
            this.errors.add(error);
        }

        /**
         * Clears all the children and errors of this element, allowing it to be reused.
         */
        public void clear() {
            this.children.clear();
            this.errors.clear();
        }

        /**
         * Collects all the errors of this element's children.
         * @param depth the depth of this element, used for formatting
         * @return a list of lines with error messages
         */
        public List<String> createReport(int depth) {
            List<String> output = new ArrayList<>();
            for (StackElement i : this.children) {
                List<String> local = new ArrayList<>();
                local.add("-".repeat(depth) + " Errors in element " + i.name + ":");
                local.addAll(i.createReport(depth + 1));
                for (String j : i.errors) {
                    local.add("-".repeat(depth) + "> " + j);
                }
                if (local.size() > 1) { // any of the children had errors, or the element has errors
                    output.addAll(local);
                }
            }
            if (depth == 0 && !this.errors.isEmpty()) {
                output.add("Errors in root element:");
                for (String i : this.errors) {
                    output.add("- " + i);
                }
            }
            return output;
        }

        /**
         * Returns the full name of this element - it's parents' names and this element's name,
         * joined by a slash ({@code /}) symbol.
         */
        public String fullName() {
            if (parent == null) return name;
            return parent.fullName() + "/" + name;
        }
    }
}
