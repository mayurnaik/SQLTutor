package edu.gatech.sqltutor.rules.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import edu.gatech.sqltutor.rules.symbolic.tokens.ISymbolicToken;

public class SymbolicTokenChildList implements List<ISymbolicToken> {
	
	private class IteratorWrapper implements Iterator<ISymbolicToken> {
		private Iterator<ISymbolicToken> iterator;
		private ISymbolicToken last;
		public IteratorWrapper(Iterator<ISymbolicToken> iterator) {
			this.iterator = iterator;
		}
		
		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}
		
		@Override
		public ISymbolicToken next() {
			return last = iterator.next();
		}
		
		@Override
		public void remove() {
			iterator.remove();
			last.setParent(null);
		}
	}

	private class ListIteratorWrapper implements ListIterator<ISymbolicToken> {
		private ListIterator<ISymbolicToken> iterator;
		private ISymbolicToken current;
		public ListIteratorWrapper(ListIterator<ISymbolicToken> iterator) {
			this.iterator = iterator;
		}
		public void add(ISymbolicToken e) {
			iterator.add(e);
		}
		public boolean hasNext() {
			return iterator.hasNext();
		}
		public boolean hasPrevious() {
			return iterator.hasPrevious();
		}
		public ISymbolicToken next() {
			return current = iterator.next();
		}
		public int nextIndex() {
			return iterator.nextIndex();
		}
		public ISymbolicToken previous() {
			return current = iterator.previous();
		}
		public int previousIndex() {
			return iterator.previousIndex();
		}
		public void remove() {
			iterator.remove();
			current.setParent(null);
		}
		public void set(ISymbolicToken e) {
			iterator.set(e);
			current.setParent(null);
			e.setParent(parent);
		}
	}
	
	ISymbolicToken parent;
	List<ISymbolicToken> wrapped;

	public SymbolicTokenChildList(ISymbolicToken parent, List<ISymbolicToken> wrapped) {
		this.parent = parent;
		this.wrapped = wrapped;
	}

	@Override
	public boolean add(ISymbolicToken e) {
		if( wrapped.add(e) ) {
			e.setParent(parent);
			return true;
		}
		return false;
	}

	@Override
	public void add(int index, ISymbolicToken element) {
		wrapped.add(index, element);
		element.setParent(parent);
	}
	
	protected void checkParents() {
		for( ISymbolicToken child: wrapped ) {
			if( child.getParent() != parent ) {
				System.out.println("Fixing parent...");
				child.setParent(parent);
			}
		}
	}

	@Override
	public boolean addAll(Collection<? extends ISymbolicToken> c) {
		boolean ret = wrapped.addAll(c);
		checkParents();
		return ret;
	}

	@Override
	public boolean addAll(int index, Collection<? extends ISymbolicToken> c) {
		boolean ret = wrapped.addAll(index, c);
		checkParents();
		return ret;
	}

	@Override
	public void clear() {
		for( ISymbolicToken child: wrapped )
			child.setParent(null);
		wrapped.clear();
	}

	@Override
	public boolean contains(Object o) {
		return wrapped.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return wrapped.containsAll(c);
	}

	@Override
	public ISymbolicToken get(int index) {
		return wrapped.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return wrapped.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return wrapped.isEmpty();
	}

	@Override
	public Iterator<ISymbolicToken> iterator() {
		return new IteratorWrapper(wrapped.iterator());
	}

	@Override
	public int lastIndexOf(Object o) {
		return wrapped.lastIndexOf(o);
	}

	@Override
	public ListIterator<ISymbolicToken> listIterator() {
		return new ListIteratorWrapper(wrapped.listIterator());
	}

	@Override
	public ListIterator<ISymbolicToken> listIterator(int index) {
		return new ListIteratorWrapper(wrapped.listIterator(index));
	}

	@Override
	public boolean remove(Object o) {
		boolean ret = wrapped.remove(o);
		if( ret )
			((ISymbolicToken)o).setParent(null);
		return ret;
	}

	@Override
	public ISymbolicToken remove(int index) {
		ISymbolicToken token = wrapped.remove(index);
		if( token != null )
			token.setParent(null);
		return null;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean removed = false;
		for( Object o: c )
			removed |= this.remove(o);
		return removed;
		
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean ret = wrapped.retainAll(c);
		return ret;
	}

	@Override
	public ISymbolicToken set(int index, ISymbolicToken element) {
		ISymbolicToken old = wrapped.set(index, element);
		if( old != null )
			old.setParent(null);
		element.setParent(parent);
		return old;
	}

	@Override
	public int size() {
		return wrapped.size();
	}

	@Override
	public List<ISymbolicToken> subList(int fromIndex, int toIndex) {
		return new SymbolicTokenChildList(parent, wrapped.subList(fromIndex, toIndex));
	}

	@Override
	public Object[] toArray() {
		return wrapped.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return wrapped.toArray(a);
	}

	
}
