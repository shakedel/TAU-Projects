package state;

import java.util.Observable;

public class CDB extends Observable {
	
	@Override
	public void notifyObservers(Object cdbTrans) {
		this.setChanged();
		super.notifyObservers(cdbTrans);
	}
	
	public static class CdbTrans {
		private final CdbId cdbId;
		private final float value;
		
		public CdbTrans(CdbId cdbId, float value) {
			this.cdbId = cdbId;
			this.value = value;
		}

		public CdbId getCdbId() {
			return cdbId;
		}

		public float getValue() {
			return value;
		}
		
	}
}
