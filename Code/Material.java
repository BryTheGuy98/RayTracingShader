package Code;

public class Material {
	private float[] matAmb;
	private float[] matDif;
	private float[] matSpe;
	private float matShi;
	
	public Material(String type) {
		float[] custAmb, custDif, custSpe;
		switch (type) {
		case "gold":
			matAmb = Utils.goldAmbient();
			matDif = Utils.goldDiffuse();
			matSpe = Utils.goldSpecular();
			matShi = Utils.goldShininess();
			break;
		case "silver":
			matAmb = Utils.silverAmbient();
			matDif = Utils.silverDiffuse();
			matSpe = Utils.silverSpecular();
			matShi = Utils.silverShininess();
			break;
		case "bronze":
			matAmb = Utils.bronzeAmbient();
			matDif = Utils.bronzeDiffuse();
			matSpe = Utils.bronzeSpecular();
			matShi = Utils.bronzeShininess();
			break;
		case "jade":
			custAmb = new float[] {
					0.1350f, 0.2225f, 0.1575f, 0.95f
			};
			custDif = new float[]{
					.5400f, 0.8900f, 0.6300f, 0.95f
			};
			custSpe = new float[] {
					0.3162f, 0.3162f, 0.3162f, 0.95f
			};
			matAmb = custAmb;
			matDif = custDif;
			matSpe = custSpe;
			matShi = 12.800f;
			break;
		case "pearl":
			custAmb = new float[] {
					0.2500f, 0.2073f, 0.2073f, 0.922f
			};
			custDif = new float[]{
					1.000f, 0.8290f, 0.8290f, 0.922f
			};
			custSpe = new float[] {
					0.2966f, 0.2966f, 0.2966f, 0.922f
			};
			matAmb = custAmb;
			matDif = custDif;
			matSpe = custSpe;
			matShi = 11.264f;
			break;
		case "custom":
			custAmb = new float[] {
					0.2500f, 0.8031f, 0.2073f, 0.934f
			};
			custDif = new float[]{
					.9011f, 0.9348f, 0.8722f, 0.934f
			};
			custSpe = new float[] {
					0.3188f, 0.3188f, 0.3188f, 0.934f
			};
			matAmb = custAmb;
			matDif = custDif;
			matSpe = custSpe;
			matShi = 12.456f;
			break;
		case "default":
			custAmb = new float[] {
					0.2f, 0.2f, 0.2f, 1.0f
			};
			custDif = new float[]{
					0.7f, 0.7f, 0.7f, 1.0f
			};
			custSpe = new float[] {
					1.0f, 1.0f, 1.0f, 1.0f
			};
			matAmb = custAmb;
			matDif = custDif;
			matSpe = custSpe;
			matShi = 50.0f;
			break;
		}
	}
	
	public float[] getAmb() { return matAmb; }
	public float[] getDif() { return matDif; }
	public float[] getSpe() { return matSpe; }
	public float getShi() { return matShi; }
}