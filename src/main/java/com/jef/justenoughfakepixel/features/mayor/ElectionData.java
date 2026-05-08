package com.jef.justenoughfakepixel.features.mayor;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ElectionData {

    public Candidate first;
    public Candidate second;
    public Candidate third;
    public Candidate fourth;
    public Candidate fifth;

    public boolean isNull() {
        return first == null || second == null || third == null || fourth == null || fifth == null;
    }
}
