package com.mygdx.holowyth.test.sandbox.raycasting;

import java.util.ArrayList;

import com.mygdx.holowyth.util.data.Segment;

public class DraggablePoint {

	ArrayList<DraggedListener> listeners = new ArrayList<DraggedListener>();

	float x, y;

	DraggablePoint(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public DraggablePoint addDraggedListener(DraggedListener listener) {
		listeners.add(listener);
		return this;
	}

	public void dragged(float x, float y) {
		this.x = x;
		this.y = y;
		for (DraggedListener listener : listeners) {
			listener.notify(x, y);
		}
	}

	public static ArrayList<DraggablePoint> getDraggablePointsFrom(Segment seg) {
		ArrayList<DraggablePoint> points = new ArrayList<>();
		points.add(new DraggablePoint(seg.x1, seg.y1).addDraggedListener((x, y) -> {
			seg.x1 = x;
			seg.y1 = y;
		}));
		points.add(new DraggablePoint(seg.x2, seg.y2).addDraggedListener((x, y) -> {
			seg.x2 = x;
			seg.y2 = y;
		}));
		return points;
	}

	public interface DraggedListener {
		public void notify(Float f1, Float f2);
	}

}
