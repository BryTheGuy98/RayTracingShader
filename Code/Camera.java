package Code;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
	private Vector3f loc;
	private Vector3f u;
	private Vector3f v;
	private Vector3f n;
	private Matrix4f view, viewR, viewT;
	
	public Camera() {
		loc = new Vector3f(0.0f, 0.0f, -5.0f);
		u = new Vector3f(1, 0, 0);
		v = new Vector3f(0, 1, 0);
		n = new Vector3f(0, 0, 1);
		view = new Matrix4f();
		viewR = new Matrix4f();
		viewT = new Matrix4f();
	}
	public Camera(Vector3f loc) {
		this.loc = loc;
		u = new Vector3f(1, 0, 0);
		v = new Vector3f(0, 1, 0);
		n = new Vector3f(0, 0, 1);
		view = new Matrix4f();
		viewR = new Matrix4f();
		viewT = new Matrix4f();
	}
	public Camera(Vector3f loc, Vector3f u, Vector3f v, Vector3f n) {
		this.loc = loc;
		this.u = u; this.v = v; this.n = n;
	}
	public void setLoc(Vector3f newLoc) { loc = newLoc; }
	public void setLoc(float x, float y, float z) { loc = new Vector3f(x, y, z); }
	public void setX(float x) { loc.x = x; }
	public void setY(float y) { loc.y = y; }
	public void setZ(float z) { loc.z = z; }
	public void setRot(Vector3f u, Vector3f v, Vector3f n) { this.u = u; this.v = v; this.n = n; }
	public void setU(Vector3f u) { this.u = u; }
	public void setV(Vector3f v) { this.v = v; }
	public void setN(Vector3f n) { this.n = n; }
	public Vector3f getLoc() { return loc; }
	public Vector3f getU() { return u; }
	public Vector3f getV() { return v; }
	public Vector3f getN() { return n; }
	
	public void yaw(float amt) {	// Rotate X
		u.rotateAxis(amt, v.x, v.y, v.z);
		n.rotateAxis(amt, v.x, v.y, v.z);
	}
	
	public void pitch(float amt) {	// rotate Y
		v.rotateAxis(amt, u.x, u.y, u.z);
		n.rotateAxis(amt, u.x, u.y, u.z);
	}
	
	public void roll(float amt) {	// Rotate Z
		u.rotateAxis(amt, n.x, n.y, n.z);
		v.rotateAxis(amt, n.x, n.y, n.z);
	}
	
	public void moveY(float amt) {	// Move Up/Down
		loc.add(new Vector3f(v).mul(amt));
	}
	
	public void moveX(float amt) { // Move Left/Right
		loc.add(new Vector3f(u).mul(amt));
	}
	
	public void moveZ(float amt) {	// Move Forward/Backward
		loc.add(new Vector3f(n).mul(amt));
	}
	
	public Matrix4f getViewMatrix() {
		viewT.set(1.0f, 0.0f, 0.0f, 0.0f,
				0.0f, 1.0f, 0.0f, 0.0f,
				0.0f, 0.0f, 1.0f, 0.0f,
				-loc.x(), -loc.y(), -loc.z(), 1.0f);

				viewR.set(u.x(), v.x(), -n.x(), 0.0f,
				u.y(), v.y(), -n.y(), 0.0f,
				u.z(), v.z(), -n.z(), 0.0f,
				0.0f, 0.0f, 0.0f, 1.0f);

				view.identity();
				view.mul(viewR);
				view.mul(viewT);

				return(view);
	}
}