package org.rogach.jopenvoronoi.util;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.rogach.jopenvoronoi.HalfEdgeDiagram;
import org.rogach.jopenvoronoi.geometry.Edge;
import org.rogach.jopenvoronoi.geometry.Point;

//\brief error-functor to locate ::SPLIT vertices
///
//for passing to numerical boost::toms748 root-finding algorithm
public class SplitPointError implements UnivariateFunction {

	private HalfEdgeDiagram g; // < reference to vd-graph
	private Edge edge; // < the HEEdge on which we position the new SPLIT vertex
	private Point p1; // < first point of the split-line
	private Point p2; // < second point of the split-line

	// \param gi graph
	// \param split_edge the edge on which we want to position a SPLIT vertex
	// \param pt1 first point of split-line
	// \param pt2 second point of split-line
	public SplitPointError(HalfEdgeDiagram gi, Edge split_edge, Point pt1, Point pt2) {
		this.g = gi;
		this.edge = split_edge;
		this.p1 = pt1;
		this.p2 = pt2;
	}

	// \return signed distance to the pt1-pt2 line from edge-point at given offset
	// \a t
	@Override
	public double value(double t) {
		var p = edge.point(t);
		// line: pt1 + u*(pt2-pt1) = p
		// (p-pt1) dot (pt2-pt1) = u* (pt2-pt1) dot (pt2-pt1)

		var u = (p.sub(p1)).dot(p2.sub(p1)) / ((p2.sub(p1)).dot(p2.sub(p1)));
		var proj = p1.add(p2.sub(p1).mult(u));
		var dist = (proj.sub(p)).norm();
		double sign;
		if (p.is_right(p1, p2)) {
			sign = +1;
		} else {
			sign = -1;
		}

		return sign * dist;
	}
};
