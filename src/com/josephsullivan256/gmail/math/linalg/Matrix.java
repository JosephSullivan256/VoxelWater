package com.josephsullivan256.gmail.math.linalg;

public class Matrix {
	private float[][] vals;
	
	public Matrix(float[][] vals) {
		this.vals = vals;
	}
	
	public Matrix(int nr, int nc, MatrixBuilder m) {
		vals = new float[nr][nc];
		
		for(int r = 0; r < nr; r++) {
			for(int c = 0; c < nc; c++) {
				vals[r][c] = m.calculate(r, c);
			}
		}
	}
	
	//identity matrix
	public static Matrix i(int n) {
		return new Matrix(
				n,n,
				(r,c)->((r==c) ? 1 : 0)
				);
	}
	
	public static final Matrix m44i = Matrix.i(4); //4x4 identity
	
	public static Matrix s44(Vec4 v) {
		return new Matrix(new float[][] {
			{v.x,0,0,0},
			{0,v.y,0,0},
			{0,0,v.z,0},
			{0,0,0,1}
		});
	}
	
	public static Matrix t44(Vec4 v) {
		return new Matrix(new float[][] {
			{1,0,0,v.x},
			{0,1,0,v.y},
			{0,0,1,v.z},
			{0,0,0,1}
		});
	}
	
	public static Matrix t44(Vec3 v) {
		return new Matrix(new float[][] {
			{1,0,0,v.x},
			{0,1,0,v.y},
			{0,0,1,v.z},
			{0,0,0,1}
		});
	}
	
	public static Matrix rx44(float cos, float sin) {
		return new Matrix(new float[][] {
			{1,0,0,0},
			{0,cos,-sin,0},
			{0,sin,cos,0},
			{0,0,0,1},
		});
	}
	
	public static Matrix rx44(float theta) {
		return Matrix.rx44((float)Math.cos(theta),(float)Math.sin(theta));
	}
	
	public static Matrix ry44(float cos, float sin) {
		return new Matrix(new float[][] {
			{cos,0,sin,0},
			{0,1,0,0},
			{-sin,0,cos,0},
			{0,0,0,1},
		});
	}
	
	public static Matrix ry44(float theta) {
		return Matrix.ry44((float)Math.cos(theta),(float)Math.sin(theta));
	}
	
	public static Matrix rz44(float cos, float sin) {
		return new Matrix(new float[][] {
			{cos,-sin,0,0},
			{sin,cos,0,0},
			{0,0,1,0},
			{0,0,0,1},
		});
	}
	
	public static Matrix rz44(float theta) {
		return Matrix.rz44((float)Math.cos(theta),(float)Math.sin(theta));
	}
	
	public static Matrix perspective(float aspect, float fov, float far, float near){
		float tan = (float) Math.tan(fov/2f);
		return new Matrix(new float[][]{
			{1f/(aspect*tan),0,0,0},
			{0,1f/tan,0,0},
			{0,0,-(far+near)/(far-near), -(2*far*near)/(far-near)},
			{0,0,-1,0}
		});
	}
	
	private static float dot(int r, int c, Matrix a, Matrix b) {
		float sum = 0f;
		for(int i = 0; i < a.vals[r].length; i++) {
			sum+=a.vals[r][i]*b.vals[i][c];
		}
		return sum;
	}
	
	public Matrix plus(Matrix m) {
		if(vals.length != m.vals.length || vals[0].length != m.vals[0].length) return null;
		return new Matrix(
				vals.length,vals[0].length,
				(r,c)->vals[r][c]+m.vals[r][c]
				);
	}
	
	public Matrix scaledBy(float s) {
		return new Matrix(
				vals.length,vals[0].length,
				(r,c)->vals[r][c]*s
				);
	}
	
	public Matrix minus(Matrix m) {
		return this.plus(m.scaledBy(-1f));
	}
	
	public Matrix times(Matrix m) {
		return new Matrix(
				vals.length,m.vals[0].length,
				(r,c)->Matrix.dot(r, c, this, m));
	}
	
	public float determinant() {
		if(vals.length!=vals[0].length) return 0;
		if(vals.length == 1) return vals[0][0];
		
		float sum = 0;
		
		for(int r = 0; r < vals.length; r++) {
			sum += cofactor(r,0)*vals[r][0];
		}
		
		return sum;
	}
	
	public float determinantR(float[] cofactors, int r) {
		float sum = 0;
		
		for(int c = 0; c < vals[0].length; c++) {
			sum += cofactor(r,c)*vals[r][c];
		}
		
		return sum;
	}
	
	public float determinantC(float[] cofactors, int c) {
		float sum = 0;
		
		for(int r = 0; r < vals.length; r++) {
			sum += cofactors[r]*vals[r][c];
		}
		
		return sum;
	}
	
	public Matrix minor(int r, int c) {
		float[][] nvals = new float[vals.length-1][vals.length-1];
		int r1 = 0;
		int c1 = 0;
		for(int r2 = 0; r2 < vals.length; r2++) {
			if(r2 == r) continue;
			for(int c2 = 0; c2 < vals.length; c2++) {
				if(c2 == c) continue;
				nvals[r1][c1] = vals[r2][c2];
				c1++;
			}
			r1++;
			c1=0;
		}
		return new Matrix(nvals);
	}
	
	public float associatedSign(int r, int c) {
		return 2*((r+c+1)%2)-1;
	}
	
	public float cofactor(int r, int c) {
		return (minor(r,c).determinant())*associatedSign(r,c);
	}
	
	public Matrix transpose() {
		return new Matrix(
				vals[0].length,vals.length,
				(r,c)->vals[c][r]);
	}
	
	public Matrix inverse() {
		Matrix cofactorMatrix = new Matrix(
				vals.length,vals[0].length,
				(r,c)->cofactor(r,c));
		float det = determinantR(cofactorMatrix.vals[0],0);
		return cofactorMatrix.transpose().scaledBy(1.0f/det);
	}
	
	public float[][] getVals() {
		return vals;
	}
	
	@Override
	public String toString() {
		String temp = "Matrix:\n";
		for(int r = 0; r < vals.length; r++) {
			temp+="[";
			for(int c = 0; c < vals[0].length; c++) {
				temp += vals[r][c];
				if(c != vals[0].length-1) temp += ",";
			}
			temp+="]";
			if(r != vals.length-1) temp+="\n";
		}
		return temp;
	}
	
	public Vec3 asVec3() {
		return new Vec3(vals[0][0],vals[1][0],vals[2][0]);
	}
}
