package ru.tinkoff.qa.neptune.selenium.functions.intreraction;

import org.openqa.selenium.interactions.Actions;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builds an action that performs the releasing of MODIFIER key.
 */
abstract class KeyUpActionSupplier extends InteractiveAction {

    final CharSequence modifierKey;

    KeyUpActionSupplier(CharSequence modifierKey) {
        super();
        checkNotNull(modifierKey);
        this.modifierKey = modifierKey;
    }

    static final class KeyUpSimpleActionSupplier extends KeyUpActionSupplier {

        KeyUpSimpleActionSupplier(CharSequence modifierKey) {
            super(modifierKey);
        }

        @Override
        void addAction(Actions value) {
            value.keyUp(modifierKey);
        }
    }

    static final class KeyUpOnElementActionSupplier extends KeyUpActionSupplier {

        private final Object e;

        KeyUpOnElementActionSupplier(CharSequence modifierKey, Object e) {
            super(modifierKey);
            checkNotNull(e);
            this.e = e;
        }

        @Override
        void addAction(Actions value) {
            value.keyUp(getElement(e), modifierKey);
        }
    }
}
