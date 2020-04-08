/*
 * Copyright (c) 2019-2020 "Neo4j,"
 * Neo4j Sweden AB [https://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.springframework.data.repository.query;

import java.util.Arrays;
import java.util.Objects;

import org.springframework.data.geo.Box;
import org.springframework.data.geo.Point;
import org.springframework.data.geo.Polygon;

/**
 * This is a utility class that computes the bounding box of a polygon as a rectangle defined by the lower left and
 * upper right point.
 *
 * @author Michael J. Simons
 * @since 1.0
 */
public final class BoundingBox {

	public static BoundingBox of(Polygon p) {

		return buildFrom(p.getPoints());
	}

	public static BoundingBox of(Box b) {

		return buildFrom(Arrays.asList(b.getFirst(), b.getSecond()));
	}

	private static BoundingBox buildFrom(Iterable<Point> points) {
		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;

		for (Point point : points) {
			minX = Math.min(point.getX(), minX);
			maxX = Math.max(point.getX(), maxX);
			minY = Math.min(point.getY(), minY);
			maxY = Math.max(point.getY(), maxY);
		}

		return new BoundingBox(new Point(minX, minY), new Point(maxX, maxY));
	}

	private final Point lowerLeft;
	private final Point upperRight;

	private BoundingBox(Point lowerLeft, Point upperRight) {
		this.lowerLeft = lowerLeft;
		this.upperRight = upperRight;
	}

	public Point getLowerLeft() {
		return lowerLeft;
	}

	public Point getUpperRight() {
		return upperRight;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		BoundingBox that = (BoundingBox) o;
		return lowerLeft.equals(that.lowerLeft) &&
			upperRight.equals(that.upperRight);
	}

	@Override
	public int hashCode() {
		return Objects.hash(lowerLeft, upperRight);
	}

	@Override
	public String toString() {
		return "BoundingBox{" +
			"ll=" + lowerLeft +
			", ur=" + upperRight +
			'}';
	}
}
