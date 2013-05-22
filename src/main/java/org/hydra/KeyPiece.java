package org.hydra;

import java.io.Serializable;
import java.math.BigInteger;

import org.joni.test.meta.AccessControlledImpl;

public class KeyPiece extends AccessControlledImpl implements Serializable {

    private static final long serialVersionUID = -397018103828829782L;
    public int pieceNumber = -1;
    public int minPieces = -1;
    public BigInteger keyPiece = null;
    public BigInteger iv = null;

}
