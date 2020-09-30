/**
 * Todo: update or remove (outdated)
 * Unit: <br>
 * Is the implementation/api for future Ai modules and the {@link Controls} class <br>
 * Implements commands like move, attackMove, attack target unit, and also controls automatic behavior of units. <br>
 * Holds information related to commands, including attackOrders and retreating. <br>
 * Handles skills and provides that lets {@link Controls} cause a unit to start casting. <br>
 * Keeps track of simple cooldowns like attacking and attack of opportunities <br>
 * 
 * Motion: <br>
 * Holds path and is responsible for the motion of the unit <br>
 * 
 * Unit Stats <br>
 * Is responsible for stat-dependent combat mechanics, such as acc, damage, hp, etc. <br>
 * In essence, all combat logic that doesn't directly interact with movement and commands would fall under this component. <br>
 * Status ailments including slows should be tracked here
 */
package com.mygdx.holowyth.unit;