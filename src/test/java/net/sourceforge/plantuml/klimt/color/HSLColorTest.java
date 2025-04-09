package net.sourceforge.plantuml.klimt.color;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HSLColorTest  {

	@ParameterizedTest
	@CsvSource({
		"   0, 100, 50, a ",
		"  45, 100, 50, a ",
		"  90, 100, 50, a ",
		" 135, 100, 50, a ",
		" 180, 100, 50, a ",
		" 225, 100, 50, a ",
		" 270, 100, 50, a ",
		" 315, 100, 50, a ",
		" 360, 100, 50, a ",
	})
	void test_toString(Integer h, Integer s, Integer l, String expectedToString) {
		assertThat(new HSLColor(h, s, l)).hasToString(expectedToString);
	}
}
