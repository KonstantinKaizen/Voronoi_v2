package org.rogach.jopenvoronoi.geometry;

import static org.rogach.jopenvoronoi.util.Numeric.chop;
import static org.rogach.jopenvoronoi.util.Numeric.sq;

import org.rogach.jopenvoronoi.site.Site;
import org.rogach.jopenvoronoi.vertex.Vertex;

/*
* bisector formulas
* x = x1 - x2 - x3*t +/- x4 * sqrt( square(x5+x6*t) - square(x7+x8*t) )
* (same formula for y-coordinate)
* line (line/line)
* parabola (circle/line)
* hyperbola (circle/circle)
* ellipse (circle/circle)
*/

public class Edge {

	public Vertex source;
	public Vertex target;
	public Edge twin;
	public Edge next;
	public Face face;
	public Face null_face;
	public boolean has_null_face;
	public double k; // < offset-direction from the adjacent site, either +1 or -1
	public EdgeType type;
	public boolean valid;
	public double[] x = new double[8]; // < 8-parameter parametrization
	public double[] y = new double[8]; // < 8-parameter parametrization
	public boolean sign; // < flag to choose either +/- in front of sqrt()
	public boolean inserted_direction; // < true if ::LINESITE-edge inserted in this direction

	public Edge(Vertex source, Vertex target) {
		x[0] = 0;
		x[1] = 0;
		x[2] = 0;
		x[3] = 0;
		x[4] = 0;
		x[5] = 0;
		x[6] = 0;
		x[7] = 0;
		y[0] = 0;
		y[1] = 0;
		y[2] = 0;
		y[3] = 0;
		y[4] = 0;
		y[5] = 0;
		y[6] = 0;
		y[7] = 0;
		has_null_face = false;
		valid = true;

		this.source = source;
		this.target = target;
	}

	public void copyFrom(Edge other) {
		this.sign = other.sign;
		this.face = other.face;
		this.k = other.k;
		this.null_face = other.null_face;
		this.has_null_face = other.has_null_face;
		this.type = other.type;
		this.valid = other.valid;
		x[0] = other.x[0];
		x[1] = other.x[1];
		x[2] = other.x[2];
		x[3] = other.x[3];
		x[4] = other.x[4];
		x[5] = other.x[5];
		x[6] = other.x[6];
		x[7] = other.x[7];
		y[0] = other.y[0];
		y[1] = other.y[1];
		y[2] = other.y[2];
		y[3] = other.y[3];
		y[4] = other.y[4];
		y[5] = other.y[5];
		y[6] = other.y[6];
		y[7] = other.y[7];
	}

	// \brief return point on edge at given offset-distance t
	///
	// the eight-parameter formula for a point on the edge is:
	// x = x1 - x2 - x3*t +/- x4 * sqrt( square(x5+x6*t) - square(x7+x8*t) )
	public Point point(double t) {
		var discr1 = chop(sq(x[4] + x[5] * t) - sq(x[6] + x[7] * t), 1e-14);
		var discr2 = chop(sq(y[4] + y[5] * t) - sq(y[6] + y[7] * t), 1e-14);
		if ((discr1 >= 0) && (discr2 >= 0)) {
			double psig = sign ? +1 : -1;
			double nsig = sign ? -1 : +1;
			var xc = x[0] - x[1] - x[2] * t + psig * x[3] * Math.sqrt(discr1);
			var yc = y[0] - y[1] - y[2] * t + nsig * y[3] * Math.sqrt(discr2);
			if (xc != xc) { // test for NaN!
				throw new RuntimeException();
			}
			return new Point(xc, yc);
		} else {
			return new Point(x[0] - x[1] - x[2] * t, y[0] - y[1] - y[2] * t); // coordinates without sqrt()
		}
	}

	/**
	 * Returns the midpoint of the edge source and target positions
	 * 
	 * @return
	 */
	public Point position() {
		return (source.position.add(target.position)).mult(0.5); // TODO pre-calculate?
	}

	// dispatch to setter functions based on type of \a s1 and \a s2
	public void set_parameters(Site s1, Site s2, boolean sig) {
		sign = sig; // sqrt() sign for edge-parametrization
		if (s1.isPoint() && s2.isPoint()) {
			set_pp_parameters(s1, s2);
		} else if (s1.isPoint() && s2.isLine()) {
			set_pl_parameters(s1, s2);
		} else if (s2.isPoint() && s1.isLine()) { // LP
			set_pl_parameters(s2, s1);
			sign = !sign;
		} else if (s1.isLine() && s2.isLine()) {
			set_ll_parameters(s2, s1);
		} else if (s1.isPoint() && s2.isArc()) {
			set_pa_parameters(s1, s2);
		} else if (s2.isPoint() && s1.isArc()) { // AP
			sign = !sign;
			set_pa_parameters(s2, s1);

		} else if (s1.isLine() && s2.isArc()) {
			set_la_parameters(s1, s2);
		} else if (s2.isLine() && s1.isArc()) {
			set_la_parameters(s2, s1);
		} else {
			throw new RuntimeException("Unexpected combination of sites");
			// AA
		}
	}

	// set edge parameters for PointSite-PointSite edge
	public void set_pp_parameters(Site s1, Site s2) {
		assert (s1.isPoint() && s2.isPoint()) : " s1.isPoint() && s2.isPoint() ";
		var d = (s1.position().sub(s2.position())).norm();
		var alfa1 = (s2.x() - s1.x()) / d;
		var alfa2 = (s2.y() - s1.y()) / d;
		var alfa3 = -d / 2;

		type = EdgeType.LINE;
		x[0] = s1.x();
		x[1] = alfa1 * alfa3; //
		x[2] = 0;
		x[3] = -alfa2;
		x[4] = 0;
		x[5] = +1;
		x[6] = alfa3;
		x[7] = 0;
		y[0] = s1.y();
		y[1] = alfa2 * alfa3;
		y[2] = 0;
		y[3] = -alfa1;
		y[4] = 0;
		y[5] = +1;
		y[6] = alfa3;
		y[7] = 0;
	}

	// set ::PARABOLA edge parameters (between PointSite and LineSite).
	public void set_pl_parameters(Site s1, Site s2) {
		assert (s1.isPoint() && s2.isLine()) : " s1.isPoint() && s2.isLine() ";

		type = EdgeType.PARABOLA;
		var alfa3 = s2.a() * s1.x() + s2.b() * s1.y() + s2.c(); // signed distance to line

		// figure out kk, i.e. offset-direction for LineSite
		x[0] = s1.x(); // xc1
		x[1] = s2.a() * alfa3; // alfa1*alfa3
		x[2] = s2.a(); // *kk; // -alfa1 = - a2 * k2?
		x[3] = s2.b(); // alfa2 = b2
		x[4] = 0; // alfa4 = r1 (PointSite has zero radius)
		x[5] = +1; // lambda1 (allways positive offset from PointSite)
		x[6] = alfa3; // alfa3= a2*xc1+b2*yc1+d2?
		x[7] = +1; // kk; // -1 = k2 side of line??

		y[0] = s1.y(); // yc1
		y[1] = s2.b() * alfa3; // alfa2*alfa3
		y[2] = s2.b(); // *kk; // -alfa2 = -b2
		y[3] = s2.a(); // alfa1 = a2
		y[4] = 0; // alfa4 = r1 (PointSite has zero radius)
		y[5] = +1; // lambda1 (allways positive offset from PointSite)
		y[6] = alfa3; // alfa3
		y[7] = +1; // kk; // -1 = k2 side of line??
	}

	// set ::SEPARATOR edge parameters
	public void set_sep_parameters(Point endp, Point p) {
		type = EdgeType.SEPARATOR;
		var dx = p.x - endp.x;
		var dy = p.y - endp.y;
		var d = p.sub(endp).norm();
		x[0] = endp.x;
		x[2] = -dx / d; // negative of normalized direction from endp to p
		y[0] = endp.y;
		y[2] = -dy / d;

		x[1] = 0;
		x[3] = 0;
		x[4] = 0;
		x[5] = 0;
		x[6] = 0;
		x[7] = 0;
		y[1] = 0;
		y[3] = 0;
		y[4] = 0;
		y[5] = 0;
		y[6] = 0;
		y[7] = 0;
	}

	// set edge parametrization for LineSite-LineSite edge (parallel case)
	public void set_ll_para_parameters(Site s1, Site s2) {
		assert (s1.isLine() && s2.isLine()) : " s1.isLine() && s2.isLine() ";
		type = EdgeType.PARA_LINELINE;

		// find a point (x1,y1) on the line s1
		// ax+by+c=0
		var x1 = 0D;
		var y1 = 0D;
		if (Math.abs(s1.a()) > Math.abs(s1.b())) {
			y1 = 0;
			x1 = -s1.c() / s1.a();
		} else {
			x1 = 0;
			y1 = -s1.c() / s1.b();
		}

		// find a point (x2,y2) on the line s2
		// ax+by+c=0
		var x2 = 0D;
		var y2 = 0D;
		if (Math.abs(s2.a()) > Math.abs(s2.b())) {
			y2 = 0;
			x2 = -s2.c() / s2.a();
		} else {
			x2 = 0;
			y2 = -s2.c() / s2.b();
		}

		// now e.g. the s2 line is given by
		// p = (x2,y2) + t*(-b2, a)
		// and we can find the projection of (x1,y1) onto s2 as
		// p1 = p2 = p0 + t*v
		var p1 = new Point(x1, y1);
		var p2 = new Point(x2, y2);
		var v = new Point(-s2.b(), s2.a());
		var t = p1.sub(p2).dot(v) / v.dot(v);
		var p1_proj = p2.add(v.mult(t));

		assert (p1.sub(p1_proj).norm() > 0) : " p1.sub(p1_proj).norm() > 0 ";

		// from this point, go a distance d/2 in the direction of the normal
		// to find a point through which the bisector passes
		x1 = x1 + p1_proj.sub(p1).x / 2;
		y1 = y1 + p1_proj.sub(p1).y / 2;
		// the tangent of the bisector (as well as the two line-sites) is a vector
		// (-b , a)

		x[0] = x1;
		x[1] = -s1.b();
		y[0] = y1;
		y[1] = s1.a();

		x[2] = 0;
		x[3] = 0;
		x[4] = 0;
		x[5] = 0;
		x[6] = 0;
		x[7] = 0;
		y[2] = 0;
		y[3] = 0;
		y[4] = 0;
		y[5] = 0;
		y[6] = 0;
		y[7] = 0;
	}

	// set edge parametrization for LineSite-LineSite edge
	public void set_ll_parameters(Site s1, Site s2) { // Held thesis p96
		assert (s1.isLine() && s2.isLine()) : " s1.isLine() && s2.isLine() ";
		type = EdgeType.LINELINE;
		var delta = s1.a() * s2.b() - s1.b() * s2.a();

		// (numerically) parallel line segments - the generic LLL solver
		// is numerically unstable for parallel cases
		if (Math.abs(delta) <= 1e-14) {
			set_ll_para_parameters(s1, s2);
			return;
		}

		assert (delta != 0) : " delta != 0 ";
		var alfa1 = (s1.b() * s2.c() - s2.b() * s1.c()) / delta;
		var alfa2 = (s2.a() * s1.c() - s1.a() * s2.c()) / delta;
		var alfa3 = -(s2.b() - s1.b()) / delta;
		var alfa4 = -(s1.a() - s2.a()) / delta;

		// point (alfa1,alfa2) is the intersection point between the line-segments
		// vector (-alfa3,-alfa4) is the direction/tangent of the bisector
		x[0] = alfa1;
		x[2] = -alfa3;
		y[0] = alfa2;
		y[2] = -alfa4;

		x[1] = 0;
		x[3] = 0;
		x[4] = 0;
		x[5] = 0;
		x[6] = 0;
		x[7] = 0;
		y[1] = 0;
		y[3] = 0;
		y[4] = 0;
		y[5] = 0;
		y[6] = 0;
		y[7] = 0;
	}

	// set edge parameters when s1 is PointSite and s2 is ArcSite
	public void set_pa_parameters(Site s1, Site s2) {
		assert (s1.isPoint() && s2.isArc()) : " s1.isPoint() && s2.isArc() ";
		// std::cout << "set_pa_parameters()\n";

		type = EdgeType.HYPERBOLA; // hyperbola or ellipse?
		var lamb2 = 1.0;

		// distance between centers
		var d = Math.sqrt((s1.x() - s2.x()) * (s1.x() - s2.x()) + (s1.y() - s2.y()) * (s1.y() - s2.y()));
		assert (d > 0) : " d > 0 ";
		if (d <= s2.r()) {
			lamb2 = -1.0;
			sign = !sign;
		}

		var alfa1 = (s2.x() - s1.x()) / d;
		var alfa2 = (s2.y() - s1.y()) / d;
		var alfa3 = (s2.r() * s2.r() - d * d) / (2 * d);
		var alfa4 = (lamb2 * s2.r()) / d;
		x[0] = s1.x();
		x[1] = alfa1 * alfa3;
		x[2] = alfa1 * alfa4;
		x[3] = alfa2;
		x[4] = 0; // r1; PointSite has zero radius
		x[5] = +1; // lamb1; allways outward offset from PointSite
		x[6] = alfa3;
		x[7] = alfa4;

		y[0] = s1.y();
		y[1] = alfa2 * alfa3;
		y[2] = alfa2 * alfa4;
		y[3] = alfa1;
		y[4] = 0; // r1; PointSite has zero radius
		y[5] = +1; // lamb1; allways outward offset from PointSite
		y[6] = alfa3;
		y[7] = alfa4;
	}

	// set edge parameters when s1 is ArcSite and s2 is LineSite
	public void set_la_parameters(Site s1, Site s2) {
		assert (s1.isLine() && s2.isArc()) : " s1.isLine() && s2.isArc() ";
		type = EdgeType.PARABOLA;
		double lamb2;
		if (s2.cw()) {
			lamb2 = +1.0;
		} else {
			lamb2 = -1.0;
		}
		var alfa1 = s1.a(); // a2
		var alfa2 = s1.b(); // b2
		var alfa3 = (s1.a() * s2.x() + s1.b() * s2.y() + s1.c());
		var alfa4 = s2.r();
		double kk = +1; // # positive line-offset
		// figure out sign?

		x[0] = s2.x();
		x[1] = alfa1 * alfa3;
		x[2] = alfa1 * kk;
		x[3] = alfa2;
		x[4] = alfa4;
		x[5] = lamb2;
		x[6] = alfa3;
		x[7] = kk;

		y[0] = s2.y();
		y[1] = alfa2 * alfa3;
		y[2] = alfa2 * kk;
		y[3] = alfa1;
		y[4] = alfa4;
		y[5] = lamb2;
		y[6] = alfa3;
		y[7] = kk;
	}

	// \return minumum t-value for this edge
	// this function dispatches to a helper-function based on the Site:s \a s1 and
	// \a s2
	// used only for positioning APEX vertices?
	public double minimum_t(Site s1, Site s2) {
		if (s1.isPoint() && s2.isPoint()) {
			return minimum_pp_t(s1, s2);
		} else if (s1.isPoint() && s2.isLine()) {
			return minimum_pl_t(s1, s2);
		} else if (s2.isPoint() && s1.isLine()) {
			return minimum_pl_t(s2, s1);
		} else if (s1.isLine() && s2.isLine()) {
			return 0;
		} else if (s1.isPoint() && s2.isArc()) {
			return minimum_pa_t(s1, s2);
		} else if (s2.isPoint() && s1.isArc()) {
			return minimum_pa_t(s2, s1);
		} else {
			throw new RuntimeException("Unexpected site types");
			// todo: AP, AL, AA
		}
	}

	// minimum t-value for LINE edge between PointSite and PointSite
	public double minimum_pp_t(Site s1, Site s2) {
		assert (s1.isPoint() && s2.isPoint()) : " s1.isPoint() && s2.isPoint() ";
		var p1p2 = s1.position().sub(s2.position()).norm();
		assert (p1p2 >= 0) : " p1p2 >=0 ";
		return p1p2 / 2; // this splits point-point edges at APEX
	}

	// minimum t-value for ::PARABOLA edge
	public double minimum_pl_t(Site s1, Site s2) {
		var mint = -x[6] / (2.0 * x[7]);
		assert (mint >= 0) : " mint >=0 ";
		return mint;
	}

	// minimum t-value for edge between PointSite and ArcSite
	public double minimum_pa_t(Site s1, Site s2) {
		assert (s1.isPoint() && s2.isArc()) : " s1.isPoint() && s2.isArc() ";
		var p1p2 = s1.position().sub(s2.apex_point(s1.position())).norm(); // - s2->r() ;
		assert (p1p2 >= 0) : " p1p2 >=0 ";
		return p1p2 / 2; // this splits point-point edges at APEX
	}

	@Override
	public String toString() {
		return String.format("E(%s>%s)", source.position, target.position);
	}
}
