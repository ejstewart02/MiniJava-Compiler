class FloatMaxArray{
    public static void main(String[] a) {
		System.out.println(new ArrayMax().start(3));
    }
}

class ArrayMax {
	float[] array;
	int size;

	public float start(int sz) {
		float maxValue;
		int initResult;

		size = sz;
		initResult = this.init();

		maxValue = this.findMax();
		return maxValue;
	}

	public int init() {
		array = new float[size];

		array[0] = 4.50;
		array[1] = 57.45;
		array[2] = 74.23;

		return 0;
	}


	public float findMax() {
		float best;
		int i;
		int nt;
		float aux;

		best = array[0];
		i = 1;
		while (i < size) {
			aux = array[i];
			if (best < aux)
				best = array[i];
			else
				nt = 0;
			i = i + 1;
		}
		return best;
	}

}