package com.github.cheesesoftware.simplelocks.connection;
import java.util.List;

import com.github.cheesesoftware.simplelocks.*;

public interface IConnection {
    public List<LockedBlock> getLockedBlocks();
    public void createLockedBlock(LockedBlock lockedBlock);
    public void removeLockedBlock(LockedBlock lockedBlock);
    public void saveLockStatus(LockedBlock lockedBlock);
}
