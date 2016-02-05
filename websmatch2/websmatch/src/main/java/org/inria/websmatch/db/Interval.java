package org.inria.websmatch.db;

public class Interval implements Comparable<Object> {
	public double min;
	public double max;

	public Interval(double m1, double m2) {
	    min = m1;
	    max = m2;
	}

	public double getMid() {
	    return (min + max) / (double) 2;
	}

	@Override
	public int compareTo(Object arg0) {
	    if (this.min == ((Interval) arg0).min)
		return 0;
	    else if (this.min < ((Interval) arg0).min)
		return -1;
	    else
		return 1;
	}

	public boolean equals(Object arg0) {
	    if (this.min == ((Interval) arg0).min)
		return true;
	    else
		return false;
	}

	public int hashCode() {
	    return (new Double(this.min).hashCode());
	}
}
