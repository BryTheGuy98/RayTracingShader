package Code;

public class Light {
	float[] globalAmbient;
	float[] lightAmbient;
	float[] lightDiffuse;
	float[] lightSpecular;
	
	public Light(boolean on) {
		globalAmbient = new float[] { 0.6f, 0.6f, 0.6f, 1.0f };
		if (on) {
			lightAmbient = new float[] { 0.1f, 0.1f, 0.1f, 1.0f };
			lightDiffuse = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
			lightSpecular = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
		}
		else {
			lightAmbient = new float[] { 0, 0, 0, 0 };
			lightDiffuse = new float[] { 0, 0, 0, 0 };
			lightSpecular = new float[] { 0, 0, 0, 0 };
		}
	}
	
	public float[] getGlobAmb() { return globalAmbient; }
	public float[] getAmb() { return lightAmbient; }
	public float[] getDif() { return lightDiffuse; }
	public float[] getSpe() { return lightSpecular; }
}