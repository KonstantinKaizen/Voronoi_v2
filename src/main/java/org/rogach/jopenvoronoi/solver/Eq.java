package org.rogach.jopenvoronoi.solver;

// notes from the Okabe-Boots-Sugihara book, page 171->:
/*
 * Distance-function.
 * R1 - region of endpoint pi1
 * R2 - region of endpoint pi2
 * R3 - region of line-segment Li
 *               dist(p,pi1) if  p in R1
 * dist(p,Li) =  dist(p,pi2) if  p in R2
 *               dist(p,Li)  if p in R3
 *
 * dist(p,Li) = distance from p to L, along perpendicular line to L
 *
 * = norm(  (x-xi1)   -  dotprod( (x-xi1), (xi2-xi1) ) / ( norm_sq(xi2-xi1) ) * (xi2,xi1) )
 *
 *
 *
 * Vertex - LineSegment
 * Bisectors:
 *  B1: point-point: line
 *  B2: point-line: parabola
 *  B3: line-line: line
 *
 *  Voronoi Edges:
 *  E1: point pi - point pj. straight line bisecting pi-pj
 *  E2: edge generated by line-segment L's endpoint pi. perpendicular to L, passing through pi
 *  E3: point pi - segment Lj. dist(E3, p) == dist(E3,Lj). Parabolic arc
 *  E4: line Li - Line Lj. straight line bisector
 *  (G): generator segment edge
 *
 *  Voronoi vertices (see p177 of Okabe book):
 *  V1: generators(pi, pj, pk). edges(E1, E1, E1)
 *     - compute using detH. This is also the circumcenter of the pi,pj,pk triangle
 *  V2: generators(pi, Lj, pj1) point, segment, segment's endpoint. edges(E1, E2, E3)   E1 and E3 are tangent at V2
 *     - ? compute by mirroring pi in the separator and use detH
 *  V3: generators(Li, pj, pk) edges(E1, E3, E3)   E3-edges have common directrix(Li)
 *  V4: generators(Li, Lj, pi1)  edges(E2, E3, E4)  E3-E4 tangent at V4
 *  V5: generators(pi, Lj, Lk) edges (E3, E3, E4)
 *  V6: generators(Li, Lj, Lk) edges(E4, E4, E4)
 *    - this is the incenter of a incircle inscribed by triangle Li,Lj,Lk (or sometiems excenter of excircle if V6 outside triangle?)
 *    - The Cartesian coordinates of the incenter are a weighted average of the coordinates of the three vertices using the side
 *       lengths of the triangle as weights. (The weights are positive so the incenter lies inside the triangle as stated above.)
 *      If the three vertices are located at (xa,ya), (xb,yb), and (xc,yc), and the sides opposite these vertices have corresponding
 *      lengths a, b, and c, then the incenter is at
 *      (a x_a + b x_b + c x_c)/ (a+b+c)
 *
 * bisector formulas
 * x = x1 - x2 - x3*t +/- x4 * sqrt( square(x5+x6*t) - square(x7+x8*t) )
 * (same formula for y-coordinate)
 * line (line/line)
 * parabola (circle/line)
 * hyperbola (circle/circle)
 * ellipse (circle/circle)
 *
 * line: a1*x + b1*y + c + k*t = 0  (t is amount of offset) k=+1 offset left of line, k=-1 right of line
 * with a*a + b*b = 1
 *
 * circle: square(x-xc) + square(y-yc) = square(r+k*t)  k=+1 for enlarging circle, k=-1 shrinking
 */
public class Eq {

	public boolean q; // < true for quadratic, false for linear
	public double a; // < a parameter of line-equation
	public double b; // < b parameter of line equation
	public double c; // < c parameter of line equation
	public double k; // < offset direction parameter

	// default ctor
	public Eq() {
		a = 0;
		b = 0;
		c = 0;
		k = 0;
		q = false;
	}

	public Eq(Eq other) {
		a = other.a;
		b = other.b;
		c = other.c;
		k = other.k;
		q = other.q;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Eq) {
			var o = (Eq) other;
			return a == o.a && b == o.b && c == o.c;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		var h = 0;
		h *= 42;
		h += Double.valueOf(a).hashCode();
		h *= 42;
		h += Double.valueOf(b).hashCode();
		h *= 42;
		h += Double.valueOf(c).hashCode();
		return h;
	}

	// subtract two equations from eachother
	public void subEq(Eq other) {
		a -= other.a;
		b -= other.b;
		c -= other.c;
		k -= other.k;
	}

	// subtraction
	public Eq sub(Eq other) {
		var res = new Eq();
		res.subEq(other);
		return res;
	}

	// access parameters through operator[]
	public double get(int idx) {
		switch (idx) {
			case 0:
				return a;
			case 1:
				return b;
			case 2:
				return k;
			default:
				throw new AssertionError();
		}
	}

	@Override
	public String toString() {
		return String.format("Eq(q=%s,a=%s,b=%s,c=%s,k=%s)", q, a, b, c, k);
	}
}
