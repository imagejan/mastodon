package net.trackmate.util.mempool;


/**
 * Note that this class is not thread-safe!
 *
 * TODO: javadoc
 *
 * @param <T>
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public abstract class MemPool< T extends MappedElement >
{
	protected final int bytesPerElement;

	protected T dataAccess;

	protected int capacity;

	/**
	 * The number of elements currently allocated in this pool.
	 */
	protected int size;

	/**
	 * The max size this pool ever had. This equals {@link #size} the number of
	 * elements in free-element-list.
	 */
	protected int allocatedSize;

	protected int firstFreeIndex;

	public MemPool( final int capacity, final int bytesPerElement )
	{
		this.bytesPerElement = Math.max( bytesPerElement, 8 );
		this.capacity = capacity;
		clear();
	}

	public void clear()
	{
		size = 0;
		allocatedSize = 0;
		firstFreeIndex = -1;
	}

	/**
	 * Get the number of elements currently allocated in this pool.
	 *
	 * @return number of elements.
	 */
	public int size()
	{
		return size;
	}

	public int create()
	{
		if ( firstFreeIndex < 0 )
			return append();
		else
		{
			final int index = firstFreeIndex;
			updateAccess( dataAccess, firstFreeIndex );
			firstFreeIndex = dataAccess.getIndex( 4 );
			return index;
		}
	}

	public void free( final int index )
	{
		if ( index >= 0 && index < allocatedSize )
		{
			updateAccess( dataAccess, index );
			final boolean isFree = dataAccess.getInt( 0 ) < 0;
			if ( ! isFree )
			{
				dataAccess.putIndex( -1, 0 );
				dataAccess.putIndex( firstFreeIndex, 4 );
				firstFreeIndex = index;
			}
		}
	}

	public abstract T createAccess();

	public abstract void updateAccess( final T access, final int index );

	protected abstract int append();

	public PoolIterator< T > iterator()
	{
		return new PoolIterator< T >( this );
	}

	public static class PoolIterator< T extends MappedElement >
	{
		private final MemPool< T > pool;

		private int nextIndex;

		private int currentIndex;

		private final T element;

		private PoolIterator( final MemPool< T > pool )
		{
			this.pool = pool;
			element = pool.createAccess();
			reset();
		}

		public void reset()
		{
			nextIndex = ( pool.allocatedSize == 0 ) ? 1 : -1;
			currentIndex = -1;
			prepareNextElement();
		}

		private void prepareNextElement()
		{
			if ( hasNext() )
			{
				while( ++nextIndex < pool.allocatedSize )
				{
					pool.updateAccess( element, nextIndex );
					final boolean isFree = element.getInt( 0 ) < 0;
					if ( ! isFree )
						break;
				}
			}
		}

		public boolean hasNext()
		{
			return nextIndex < pool.allocatedSize;
		}

		public int next()
		{
			currentIndex = nextIndex;
			prepareNextElement();
			return currentIndex;
		}

		public void remove()
		{
			if ( currentIndex >= 0 )
				pool.free( currentIndex );
		}
	}

	public static interface Factory< T extends MappedElement >
	{
		public MemPool< T > createPool( final int capacity, final int bytesPerElement );
	}
}
