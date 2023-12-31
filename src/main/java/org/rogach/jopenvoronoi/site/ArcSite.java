package org.rogach.jopenvoronoi.site;

import org.rogach.jopenvoronoi.geometry.Edge;
import org.rogach.jopenvoronoi.geometry.Point;
import org.rogach.jopenvoronoi.offset.ArcOfs;
import org.rogach.jopenvoronoi.offset.Ofs;
import org.rogach.jopenvoronoi.util.Numeric;

//circular arc Site
public class ArcSite extends Site {

	Point _start; // < start Point of arc
	Point _end; // < end Point of arc
	Point _center; // < center Point of arc
	boolean _dir; // < CW or CCW direction flag
	double _radius;// < radius of arc
	double _k; // < offset-direction. +1 for enlarging, -1 for shrinking circle
	Edge e; // < edge_descriptor to ::ARCSITE pseudo-edge

	// create arc-site
	public ArcSite(Point startpt, Point endpt, Point centr, boolean dir) {
		this._start = startpt;
		this._end = endpt;
		this._center = centr;
		this._dir = dir;
		this._radius = _center.sub(_start).norm();
		eq.q = true;
		eq.a = -2 * _center.x;
		eq.b = -2 * _center.y;
		_k = 1;
		eq.k = -2 * _k * _radius;
		eq.c = _center.x * _center.x + _center.y * _center.y - _radius * _radius;
	}

	@Override
	public Ofs offset(Point p1, Point p2) {
		return new ArcOfs(p1, p2, _center, -1);
	} // FIXME: radius

	@Override
	public boolean in_region(Point p) {
		if (p == _center) {
			return true;
		}

		var t = in_region_t(p);
		return ((t >= 0) && (t <= 1));
	}

	// \todo fix arc-site in_region_t test!!
	@Override
	public double in_region_t(Point pt) {
		var t = in_region_t_raw(pt); // (diangle_pt - diangle_min) / (diangle_max-diangle_min);
		var eps = 1e-7;
		if (Math.abs(t) < eps) {
			t = 0.0;
		} else if (Math.abs(t - 1.0) < eps) {
			t = 1.0;
		}
		return t;
	}

	@Override
	public double in_region_t_raw(Point pt) {
		// projection onto circle
		var cen_start = _start.sub(_center);
		var cen_end = _end.sub(_center);
		var cen_pt = pt.sub(_center);
		var proj = _center.add(cen_pt.mult(_radius / cen_pt.norm()));

		double diangle_min;
		double diangle_max;
		if (!_dir) {
			diangle_min = Numeric.diangle(cen_start.x, cen_start.y);
			diangle_max = Numeric.diangle(cen_end.x, cen_end.y);
		} else {
			diangle_max = Numeric.diangle(cen_start.x, cen_start.y);
			diangle_min = Numeric.diangle(cen_end.x, cen_end.y);
		}
		var diangle_pt = Numeric.diangle(cen_pt.x, cen_pt.y);

		var t = (diangle_pt - diangle_min) / (diangle_max - diangle_min);
		return t;
	}

	@Override
	public Point apex_point(Point p) {
		if (in_region(p)) {
			return projection_point(p);
		} else {
			return closer_endpoint(p);
		}
	}

	@Override
	public double x() {
		return _center.x;
	}

	@Override
	public double y() {
		return _center.y;
	}

	@Override
	public double r() {
		return _radius;
	}

	@Override
	public double k() {
		return _k;
	} // ?

	// return start Point of ArcSite
	@Override
	public Point start() {
		return _start;
	}

	// return end Point of ArcSite
	@Override
	public Point end() {
		return _end;
	}

	// return center Point of ArcSite
	public Point center() {
		return _center;
	}

	@Override
	public Point position() {
		return center();
	}

	// return radius of ArcSite
	public double radius() {
		return _radius;
	}

	// return true for CW ArcSite and false for CCW
	@Override
	public boolean cw() {
		return _dir;
	}

	@Override
	public boolean isArc() {
		return true;
	}

	// projection of given Point onto the ArcSite
	private Point projection_point(Point p) {
		if (p == _center) {
			return _start;
		} else {
			var dir = p.sub(_center);
			dir.normalize();
			return _center.add(dir.mult(_radius)); // this point should lie on the arc
		}
	}

	// return the end Point (either _start or _end) that is closest to the given
	// Point
	private Point closer_endpoint(Point p) {
		var d_start = _start.sub(p).norm();
		var d_end = _end.sub(p).norm();
		if (d_start < d_end) {
			return _start;
		} else {
			return _end;
		}
	}
};
