package Code;

import javax.swing.*;

import static com.jogamp.opengl.GL.GL_ARRAY_BUFFER;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FLOAT;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_STATIC_DRAW;
import static com.jogamp.opengl.GL.GL_TEXTURE0;
import static com.jogamp.opengl.GL.GL_TEXTURE_2D;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL4.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.*;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.*;
import com.jogamp.common.nio.Buffers;

import org.joml.*;
import java.lang.Math;

public class Code extends JFrame implements GLEventListener, KeyListener
{	private GLCanvas myCanvas;
	private int screenQuadShader, raytraceShader;
	
	private int raytraceRenderWidth = 512;
	private int raytraceRenderHeight = 512;
	private int workGroupsX = raytraceRenderWidth;
	private int workGroupsY = raytraceRenderHeight;
	private int workGroupsZ = 1;
	
	private int[] screenTextureID = new int[1];
	private byte[] screenTexture = new byte[raytraceRenderWidth * raytraceRenderHeight * 16];
	
	private int vao[] = new int[1]; // TODO: update
	private int vbo[] = new int[2]; // TODO: update
	private long startTime = System.currentTimeMillis();

	public Code() {
		setTitle("Ray Tracing");
		setSize(raytraceRenderWidth, raytraceRenderHeight);
		myCanvas = new GLCanvas();
		myCanvas.addGLEventListener(this);
		this.add(myCanvas);
		this.setVisible(true);
		Animator animator = new Animator(myCanvas);
		animator.start();
		myCanvas.addKeyListener(this);
	}
	
	public void keyPressed(KeyEvent e) {	// reads key input and runs respective commands
		int keyCode = e.getKeyCode();
	//	System.out.println(keyCode);
		switch (keyCode) {
		}
	}
	

	public void display(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
	
		// Phase 1: run the ray tracing compute shader
		gl.glUseProgram(raytraceShader);
		
		// bind the screen_texture_id texture to an image unit as the compute shader's output
		gl.glBindImageTexture(0, screenTextureID[0], 0, false, 0, GL_WRITE_ONLY, GL_RGBA8);
		
		// start the compute shader
		gl.glDispatchCompute(workGroupsX, workGroupsY, workGroupsZ);
		gl.glFinish();	// Wait until shader is fully complete
		
		// Phase 2: draw the resulting texture to the screen
		gl.glUseProgram(screenQuadShader);
		gl.glClear(GL_COLOR_BUFFER_BIT);
		gl.glClear(GL_DEPTH_BUFFER_BIT);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]);
		gl.glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[1]);
		gl.glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		gl.glEnableVertexAttribArray(1);
		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glBindTexture(GL_TEXTURE_2D, screenTextureID[0]);
		
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LEQUAL);
		gl.glDrawArrays(GL_TRIANGLES, 0, 6);
	}
	
	public void init(GLAutoDrawable drawable)
	{	GL4 gl = (GL4) GLContext.getCurrentGL();
		// initialize the display texture to bright pink, to make it easy to spot errors
		for (int i = 0; i < raytraceRenderHeight; ++i) {	
			for (int j = 0; j < raytraceRenderWidth; ++j) {
				screenTexture[i * raytraceRenderWidth * 4 + j * 4 + 0] = (byte) 250;
				screenTexture[i * raytraceRenderWidth * 4 + j * 4 + 1] = (byte) 128;
				screenTexture[i * raytraceRenderWidth * 4 + j * 4 + 2] = (byte) 255;
				screenTexture[i * raytraceRenderWidth * 4 + j * 4 + 3] = (byte) 255;
			}
		}
		ByteBuffer screenTexBuffer = Buffers.newDirectByteBuffer(screenTexture);
		
		// create the display texture
		gl.glGenTextures(1,  screenTextureID, 0);
		gl.glBindTexture(GL_TEXTURE_2D, screenTextureID[0]);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, raytraceRenderWidth, raytraceRenderHeight, 
				0, GL_RGBA, GL_UNSIGNED_BYTE, screenTexBuffer);
		
		// quad vertices for rendering the final texture to the window
		float[] fullscreenQuadVerts =
			{	-1.0f, 1.0f, 0.0f,		-1.0f, -1.0f, 0.0f,		1.0f, -1.0f, 0.0f,
				1.0f, -1.0f, 0.0f,		1.0f, 1.0f, 0.0f,		-1.0f, 1.0f, 0.0f 	};
		float[] fullscreenQuadUVs = 
			{	0.0f, 1.0f, 0.0f, 		0.0f, 1.0f, 0.0f,
				1.0f, 0.0f, 1.0f, 		1.0f, 0.0f, 1.0f	};
		
		gl.glGenVertexArrays(vao.length, vao, 0);
		gl.glBindVertexArray(vao[0]);
		gl.glGenBuffers(vbo.length, vbo, 0);
		
		gl.glBindBuffer(GL_ARRAY_BUFFER, vbo[0]); // vertex positions
		FloatBuffer quadVertsBuf = Buffers.newDirectFloatBuffer(fullscreenQuadVerts);
		gl.glBufferData(GL_ARRAY_BUFFER, quadVertsBuf.limit()*4, quadVertsBuf, GL_STATIC_DRAW);
		gl.glBindBuffer(GL_ARRAY_BUFFER,  vbo[1]); // texture coords
		FloatBuffer quadUVsBuf = Buffers.newDirectFloatBuffer(fullscreenQuadUVs);
		gl.glBufferData(GL_ARRAY_BUFFER, quadUVsBuf.limit()*4, quadUVsBuf, GL_STATIC_DRAW);
		
		screenQuadShader = Utils.createShaderProgram("Code/vertShader.glsl", "Code/fragShader.glsl");
		raytraceShader = Utils.createShaderProgram("Code/raytraceComputeShader.glsl");
	}
	
	public static void main(String[] args) { new Code(); }
	
	public void dispose(GLAutoDrawable drawable) {}
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}	
	public void keyTyped(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	
}