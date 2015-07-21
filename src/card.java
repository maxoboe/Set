/*
 * The card class contains information about the shade, shape, and color of a card.
 */
public class card {

	private int count; // 1,2,3
	private char shade; // s(olid), m(ixed), e(mpty)
	private char shape; // d(iamond), w(avy), o(void)
	private char color; // p(urple), g(reen), r(ed)
	private boolean selected = false; // See if human has selected
	private boolean used = false; // See if recursive algorithm has selected

	public card(int count, char shade, char shape, char color) {
		this.count = count;
		this.shade = shade;
		this.shape = shape;
		this.color = color;
	}

	public int getCount() {
		return count;
	}

	public char getShade() {
		return shade;
	}

	public char getShape() {
		return shape;
	}

	public char getColor() {
		return color;
	}

	public boolean isSelected() {
		return selected;
	}

	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean newUsed) {
		used = newUsed;
	}

	public String toString() {
		return "" + shade + shape + color;
	}

	public void setSelected(boolean newPlayed) {
		selected = newPlayed;
	}

}
