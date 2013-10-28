package edu.gatech.sqltutor.util;

public class Pair<FirstType, SecondType> {
	public static <F,S> Pair<F,S> make(F first, S second) {
		return new Pair<F,S>(first, second);
	}
	
	private FirstType first;
	private SecondType second;
	
	public Pair(FirstType first, SecondType second) {
		this.first = first;
		this.second = second;
	}

	public FirstType getFirst() {
		return first;
	}

	public void setFirst(FirstType first) {
		this.first = first;
	}

	public SecondType getSecond() {
		return second;
	}

	public void setSecond(SecondType second) {
		this.second = second;
	}

	// auto-generated
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}

	// auto-generated
	@Override
	public boolean equals(Object obj) {
		if( this == obj )
			return true;
		if( obj == null )
			return false;
		if( getClass() != obj.getClass() )
			return false;
		Pair other = (Pair)obj;
		if( first == null ) {
			if( other.first != null )
				return false;
		} else if( !first.equals(other.first) )
			return false;
		if( second == null ) {
			if( other.second != null )
				return false;
		} else if( !second.equals(other.second) )
			return false;
		return true;
	}
}
