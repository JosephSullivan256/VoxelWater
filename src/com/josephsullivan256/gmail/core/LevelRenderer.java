package com.josephsullivan256.gmail.core;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import com.josephsullivan256.gmail.gl.BufferObject;
import com.josephsullivan256.gmail.gl.Shader;
import com.josephsullivan256.gmail.gl.Texture;
import com.josephsullivan256.gmail.gl.Uniform;
import com.josephsullivan256.gmail.gl.UniformPasser;
import com.josephsullivan256.gmail.gl.VertexArrayObject;
import com.josephsullivan256.gmail.gl.VertexAttributes;
import com.josephsullivan256.gmail.math.linalg.Matrix;
import com.josephsullivan256.gmail.math.linalg.Vec3;
import com.josephsullivan256.gmail.render.SceneInfo;
import com.josephsullivan256.gmail.util.Geometry;
import com.josephsullivan256.gmail.util.Pair;

public class LevelRenderer {
	
	private Consumer<Pair<Matrix,Matrix>> drawVao0;
	private BufferObject vbo0;
	private int length;
	
	public LevelRenderer(Level lvl, SceneInfo si, Shader blockShader, Texture tex){
		
		float[] cubeVertices = Geometry.cubeVertices;
		
		VertexArrayObject vao0 = new VertexArrayObject();
		Vec3[] offsets0 = offsetsFromLevel(lvl.filled());
		length = offsets0.length;
		{
			vbo0 = BufferObject.vbo().bind().bufferData(offsets0, GL15.GL_DYNAMIC_DRAW); //maybe stream draw?
			
			vao0.initialize(
					Pair.init(
							BufferObject.vbo().bind().bufferData(cubeVertices, GL15.GL_STATIC_DRAW),
							new VertexAttributes()
							.with(3, GL11.GL_FLOAT)
							.with(3, GL11.GL_FLOAT)
							.with(2, GL11.GL_FLOAT)
							),
					Pair.init(
							vbo0,//BufferObject.vbo().bind().bufferData(offsets0, GL15.GL_STATIC_DRAW),
							new VertexAttributes()
							.withInstanced(3, GL11.GL_FLOAT)
							)
					);
		}
		
		//instantiate uniform classes
		Uniform<Vec3> sunDirection, sunAmbient, sunDiffuse, sunSpecular;
		sunDirection = new Uniform<Vec3>("sun.direction",UniformPasser.uniform3f);
		sunAmbient = new Uniform<Vec3>("sun.ambient",UniformPasser.uniform3f);
		sunDiffuse = new Uniform<Vec3>("sun.diffuse",UniformPasser.uniform3f);
		sunSpecular = new Uniform<Vec3>("sun.specular",UniformPasser.uniform3f);
		
		Uniform<Matrix> transformUniform = new Uniform<Matrix>("transform",UniformPasser.uniformMatrix4);
		Uniform<Matrix> perspectiveUniform = new Uniform<Matrix>("perspective",UniformPasser.uniformMatrix4);
		Uniform<Integer> texUniform = new Uniform<Integer>("noise",UniformPasser.uniform1i);
		
		//render consumers
		drawVao0 = (m)->{
			vao0.bind();
			blockShader.use();
			
			//uniforms
			transformUniform.uniform(m.a, blockShader);
			perspectiveUniform.uniform(m.b, blockShader);
			
			//assign texture to unit
			tex.assignToUnit(0);
			
			//glPolygonMode( GL_FRONT_AND_BACK, GL_LINE );
			vao0.drawArraysInstanced(Geometry.cubeVertices.length/8, length);
			vao0.unbind();
		};
		
		//initial uniforms
		blockShader.use();
		texUniform.uniform(0, blockShader);
		sunDirection.uniform(si.sun.dir, blockShader);
		sunAmbient.uniform(si.sun.amb, blockShader);
		sunDiffuse.uniform(si.sun.diff, blockShader);
		sunSpecular.uniform(si.sun.spec, blockShader);
	}
	
	public void update(float[][][] waterLevels){
		Vec3[] offsets = offsetsFromLevel(waterLevels);
		length = offsets.length;
		vbo0.bufferSubData(offsets);
	}
	
	public void render(Matrix viewModel, Matrix perspective){
		Pair<Matrix,Matrix> vmp = Pair.init(viewModel, perspective);
		
		drawVao0.accept(vmp);
	}
	
	private static final float scale = 1f;
	
	public static Vec3[] offsetsFromLevel(float[][][] level){
		List<Vec3> offsets = new ArrayList<Vec3>();
		
		for(int x = 0; x < level.length; x++){
			for(int y = 0; y < level[0].length; y++){
				for(int z = 0; z < level[0][0].length; z++){
					if(level[x][y][z]>0.4f) offsets.add(new Vec3(x,y,z).scaledBy(scale));
				}
			}
		}
		
		return offsets.toArray(new Vec3[offsets.size()]);
	}
}
