package org.rogach.jopenvoronoi.generate;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Random polygon generator uses space partitioning algorithm, described here:
 * http://www.geometrylab.de/applet-29-en#space
 */
public class RandomPolygon {

	public static List<Point2D> generate_points(int nPoints, int min, int max) {

		List<Point2D> points = new ArrayList<>();
		for (var i = 0; i < nPoints; i++) {
			points.add(new Point2D.Double(ThreadLocalRandom.current().nextDouble(min, max),
					ThreadLocalRandom.current().nextDouble(min, max)));
		}
		return points;
	}

	/**
	 * 
	 * @param nPoints
	 * @param min     minimum bound of x and y coordinates
	 * @param max     maximum bound of x and y coordinates
	 * @return
	 */
	public static List<Point2D> generate_polygon(int nPoints, int min, int max) {
		var points = generate_points(nPoints, min, max);
		var p1 = removeRandom(points);
		var p2 = removeRandom(points);

		List<Point2D> group1 = new ArrayList<>();
		List<Point2D> group2 = new ArrayList<>();
		partition(points, p1, p2, group1, group2);

		var path1 = buildPath(p1, p2, group1);
		var path2 = buildPath(p2, p1, group2);

		List<Point2D> result = new ArrayList<>();
		result.add(p1);
		result.addAll(path1);
		result.add(p2);
		result.addAll(path2);
		return result;
	}

	private static List<Point2D> buildPath(Point2D p1, Point2D p2, List<Point2D> points) {
		if (points.isEmpty()) {
			return new ArrayList<>();
		} else {
			var c = removeRandom(points);
			var c2 = randomBetween(p1, p2);

			List<Point2D> group1 = new ArrayList<>();
			List<Point2D> group2 = new ArrayList<>();
			partition(points, c2, c, group1, group2);

			List<Point2D> result = new ArrayList<>();
			if (isLeft(c2, c, p1)) {
				result.addAll(buildPath(p1, c, group1));
				result.add(c);
				result.addAll(buildPath(c, p2, group2));
			} else {
				result.addAll(buildPath(p1, c, group2));
				result.add(c);
				result.addAll(buildPath(c, p2, group1));
			}
			return result;
		}
	}

	/** Split points into two groups by p1-p2 line */
	private static void partition(List<Point2D> points, Point2D p1, Point2D p2, List<Point2D> group1,
			List<Point2D> group2) {
		for (Point2D p : points) {
			if (isLeft(p1, p2, p)) {
				group1.add(p);
			} else {
				group2.add(p);
			}
		}
	}

	private static Point2D randomBetween(Point2D p1, Point2D p2) {
		var t = Math.random();
		return new Point2D.Double(p1.getX() * t + p2.getX() * (1 - t), p1.getY() * t + p2.getY() * (1 - t));
	}

	private static boolean isLeft(Point2D l1, Point2D l2, Point2D p) {
		var lx = l2.getX() - l1.getX();
		var ly = l2.getY() - l1.getY();
		return lx * (p.getY() - l1.getY()) - ly * (p.getX() - l1.getX()) > 0;
	}

	private static Point2D removeRandom(List<Point2D> pts) {
		return pts.remove(new Random().nextInt(pts.size()));
	}
}