package com.quickveggies.misc;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TextField;

/**
 * Custom text field allowing only the (optionally limited) numeric characters
 * to be entered. For length restriction please java doc at
 * <code>setMaxLength()</code>
 * 
 * @author Shoeb
 *
 */
public class BoundedNumericTextField extends TextField {

	private IntegerProperty maxLength = new SimpleIntegerProperty(this, "maxLength");

	public BoundedNumericTextField() {
		maxLength.set(0);

	}

	/**
	 * Permits entry of restricted characters
	 */
	public void replaceText(int start, int end, String text) {
		String preValue = getText();
		if (hasValidText(text)) {
			super.replaceText(start, end, text);
		}
		if (isUnderLimit()) {
			setText(preValue);
		}
	}

	/**
	 * Limits pasting of restricted characters
	 * 
	 * {@inheritDoc}
	 */
	public void replaceSelection(String text) {
		String preValue = getText();
		if (hasValidText(text)) {
			super.replaceSelection(text);
		}
		if (isUnderLimit()) {
			setText(preValue);
		}
	}

	/**
	 * Checks if the number is limited
	 * 
	 * @return true if maxLength is not set, and also true if length is less
	 *         than specified max length; false otherwise
	 */
	private boolean isUnderLimit() {
		boolean isUnderLimit = false;
		if (maxLength.get() == 0) {
			isUnderLimit = false;
		} else {
			if (getText().length() > maxLength.getValue()) {
				isUnderLimit = true;
			}
		}
		return isUnderLimit;
	}

	/*
	 * Returns if entered text is valid
	 */
	private boolean hasValidText(String text) {
		boolean isValidText = true;
		if (text.matches("[A-Za-z]") || text.matches("[\\\\!\"#$%&()*+,./:;<=>?@\\[\\]^_{|}~]+")) {
			isValidText = false;
		}
		return isValidText;
	}

	/**
	 * @return the Maximum length supported by this field
	 */
	public Integer getMaxLength() {
		return maxLength.getValue();
	}

	/**
	 * If the maxLength property is set, then it will limit the number of
	 * characters can be entered. A value of 0 means no limits. Negative number
	 * will throw exceptions In case of no limit is set, only the
	 * Integer.MAX_VALUE restriction may apply.
	 * 
	 * @param maxLength
	 *            the maximum supported length to set
	 * @throws IllegalArgumentException
	 *             - if length is negative
	 */
	public void setMaxLength(Integer ml) {
		if (ml.intValue() < 0) {
			throw new IllegalArgumentException("Maximum length cannot be less than 0");
		}
		maxLength.setValue(ml);
	}

	public IntegerProperty maxLengthProperty() {
		return maxLength;
	}

}
