package com.mygdx.holowyth.unit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mygdx.holowyth.unit.UnitOrders.Order;
import com.mygdx.holowyth.util.exceptions.HoloAssertException;

/**
 * Allow a unit to store one order, which will be executed after the unit stops being stunned.
 */
class UnitOrdersDeferring {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Unit self;
	private final UnitOrders orders;

	/**
	 * When a unit stops being stunned, the unit will try to adopt this order. Should not be null.
	 */
	private Order deferredOrder = Order.NONE;
	private Unit deferredOrderTarget; // if the order specifies it
	private float deferredOrderX;
	private float deferredOrderY;

	UnitOrdersDeferring(Unit unit, UnitOrders orders) {
		self = unit;
		this.orders = orders;
	}

	void tryToResumeDeferredOrder() {

		switch (deferredOrder) {
		case ATTACKMOVE:
			self.orderAttackMove(deferredOrderX, deferredOrderY);
			break;
		case MOVE:
			self.orderMove(deferredOrderX, deferredOrderY);
			break;
		case ATTACKUNIT_HARD:
			self.orderAttackUnit(deferredOrderTarget, true);
			break;
		case ATTACKUNIT_SOFT:
			self.orderAttackUnit(deferredOrderTarget, false);
			break;
		case RETREAT:
			self.orderMove(deferredOrderX, deferredOrderY); // a unit that is stunned should already not be attacking, thus a move order is sufficient
			deferredOrder = Order.MOVE;
			break;
		case NONE:
			break;
		default:
			throw new HoloAssertException("Unhandled order type");
		}
		deferredOrder = Order.NONE;
		deferredOrderTarget = null;
		deferredOrderX = 0;
		deferredOrderY = 0;
	}

	/**
	 * Caches the given order so it can try to resume when the stun expires
	 * 
	 * @param order
	 * @param target
	 *            Optional, if the order requires it
	 * @param x
	 *            Optional
	 * @param y
	 */
	void tryToDeferOrder(Order order, Unit target, float x, float y) {

		logger.debug("{} Deferring order: {} {} {} {}", self.getName(), order.toString(), target != null ? target.getName() : "null", x, y);

		clearDeferredOrder();

		deferredOrder = order;
		switch (order) {
		case ATTACKMOVE:
			deferredOrderX = x;
			deferredOrderY = y;
			break;
		case MOVE:
			deferredOrderX = x;
			deferredOrderY = y;
			break;

		case ATTACKUNIT_HARD:
		case ATTACKUNIT_SOFT:
			deferredOrderTarget = target;
			break;

		case RETREAT:
			deferredOrderX = x;
			deferredOrderY = y;
			break;

		case NONE:
			break;
		default:
			throw new HoloAssertException("Unhandled order type");
		}
	}

	/**
	 * Caches the current order so it can try to resume when the stun expires
	 */
	void deferCurrentOrder() {

		clearDeferredOrder();

		switch (self.getOrder()) {
		case ATTACKMOVE:
			tryToDeferOrder(self.getOrder(), null, orders.getAttackMoveDestX(), orders.getAttackMoveDestY());
			break;
		case MOVE:
			tryToDeferOrder(self.getOrder(), null, self.motion.getDest().x, self.getMotion().getDest().y);
			break;
		case ATTACKUNIT_HARD:
		case ATTACKUNIT_SOFT:
			tryToDeferOrder(self.getOrder(), self.getOrderTarget(), 0, 0);
			break;
		case RETREAT:
			tryToDeferOrder(self.getOrder(), self.getOrderTarget(), self.getMotion().getDest().x, self.getMotion().getDest().y);
			break;
		case NONE:
			break;
		default:
			throw new HoloAssertException("Unhandled order type");
		}

	}

	void clearDeferredOrder() {
		deferredOrder = Order.NONE;
		deferredOrderTarget = null;
		deferredOrderX = 0;
		deferredOrderY = 0;
	}

}
