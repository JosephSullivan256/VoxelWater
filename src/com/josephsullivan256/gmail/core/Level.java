package com.josephsullivan256.gmail.core;

import java.util.ArrayList;

import com.josephsullivan256.gmail.math.linalg.Vec3;
import com.josephsullivan256.gmail.math.linalg.Vec3i;

public class Level {
	
	private float[][][] waterLevels, oldWaterLevels;
	private Vec3[][][] waterVel, oldWaterVel;
	private final int w,h,l;
	
	public Level(int w, int h, int l){
		this(randomLevel(w,h,l));
	}
	
	private static float[][][] randomLevel(int w, int h, int l){
		float[][][] waterLevels = new float[w][h][l];
		for(int x = 0; x < waterLevels.length; x++){
			for(int y = 0; y < waterLevels[0].length; y++){
				for(int z = 0; z < waterLevels[0][0].length; z++){
					waterLevels[x][y][z] = (float) Math.random();
				}
			}
		}
		return waterLevels;
	}
	
	public Level(float[][][] waterLevels){
		this.w = waterLevels.length;
		this.h = waterLevels[0].length;
		this.l = waterLevels[0][0].length;
		this.waterLevels = waterLevels;
		this.waterVel = new Vec3[waterLevels.length][waterLevels[0].length][waterLevels[0][0].length];
		for(int x = 0; x < w; x++){
			for(int y = 0; y < h; y++){
				for(int z = 0; z < l; z++){
					waterVel[x][y][z] = Vec3.zero;
				}
			}
		}
	}
	
	//physics constants
	private static final Vec3 gravity = new Vec3(0,-9.8f,0);
	
	public void update(float timeStep){
		oldWaterLevels = waterLevels;
		oldWaterVel = waterVel;
		resetWaterLevels();
		resetWaterVelocities();
		for(int x = 0; x < w; x++){
			for(int y = 0; y < h; y++){
				for(int z = 0; z < l; z++){
					add(new Vec3(x,y,z).plus(oldWaterVel[x][y][z]),oldWaterLevels[x][y][z],oldWaterVel[x][y][z].plus(gravity.scaledBy(timeStep)));
					
					
					
				}
			}
		}
	}
	
	public void resetWaterLevels(){
		waterLevels = new float[w][h][l];
	}
	public void resetWaterVelocities(){
		waterVel = new Vec3[w][h][l];
		for(int x = 0; x < w; x++){
			for(int y = 0; y < h; y++){
				for(int z = 0; z < l; z++){
					waterVel[x][y][z] = Vec3.zero;
				}
			}
		}
	}
	
	public void add(Vec3 pos, float level, Vec3 newVel){
		pos = pos.plus(newVel);
		Vec3i ufri = pos.ufr(); Vec3 ufr = ufri.vec3();
		Vec3i ufli = pos.ufl(); Vec3 ufl = ufli.vec3();
		Vec3i ubri = pos.ubr(); Vec3 ubr = ubri.vec3();
		Vec3i ubli = pos.ubl(); Vec3 ubl = ubli.vec3();
		Vec3i dfri = pos.dfr(); Vec3 dfr = dfri.vec3();
		Vec3i dfli = pos.dfl(); Vec3 dfl = dfli.vec3();
		Vec3i dbri = pos.dbr(); Vec3 dbr = dbri.vec3();
		Vec3i dbli = pos.dbl(); Vec3 dbl = dbli.vec3();
		
		float ufrw = Math.abs(dbl.minus(pos).product());
		float uflw = Math.abs(dbr.minus(pos).product());
		float ubrw = Math.abs(dfl.minus(pos).product());
		float ublw = Math.abs(dfr.minus(pos).product());
		float dfrw = Math.abs(ubl.minus(pos).product());
		float dflw = Math.abs(ubr.minus(pos).product());
		float dbrw = Math.abs(ufl.minus(pos).product());
		float dblw = Math.abs(ufr.minus(pos).product());
		
		add(ufri,ufrw,newVel);
		add(ufli,uflw,newVel);
		add(ubri,ubrw,newVel);
		add(ubli,ublw,newVel);
		add(dfri,dfrw,newVel);
		add(dfli,dflw,newVel);
		add(dbri,dbrw,newVel);
		add(dbli,dblw,newVel);
	}
	
	public float get(Vec3i pos){
		return waterLevels[pos.x][pos.y][pos.z];
	}
	
	public void set(Vec3i pos, float value){
		if(inBounds(pos)) waterLevels[pos.x][pos.y][pos.z] = value;
	}
	
	public void add(Vec3i pos, float value, Vec3 vel){
		if(inBounds(pos)){
			float oldVal = waterLevels[pos.x][pos.y][pos.z];
			waterVel[pos.x][pos.y][pos.z] = waterVel[pos.x][pos.y][pos.z].scaledBy(oldVal).plus(vel.scaledBy(value)).scaledBy(1/(value+oldVal));
			waterLevels[pos.x][pos.y][pos.z] += value;
		}
	}
	
	public boolean inBounds(Vec3i pos){
		return (0<pos.x && pos.x<w) && (0<pos.y && pos.y<h) && (0<pos.z && pos.z<l);
	}
	
	public float[][][] getWaterLevels(){
		return waterLevels;
	}
	
	public float[][][] filled(){
		float[][][] filled = new float[w][l][h];
		for(int x = 0; x < w; x++){
			for(int y = 0; y < h; y++){
				for(int z = 0; z < l; z++){
					filled[x][y][z] = 1;
				}
			}
		}
		return filled;
	}
}
