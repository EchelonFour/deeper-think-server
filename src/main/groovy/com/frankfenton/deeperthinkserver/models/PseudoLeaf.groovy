package com.frankfenton.deeperthinkserver.models

import groovy.transform.ToString
import org.bson.types.ObjectId
import org.mongodb.morphia.annotations.Embedded

/**
 * Created by EchelonFour on 6/03/2017.
 */
@Embedded
@ToString(includeNames=true)
class PseudoLeaf {

    ObjectId real
    String word
    String lemma
    Tag tag
    Label label
    int treeSize
    Label parentLabel

    public PseudoLeaf() {

    }
    public PseudoLeaf(Leaf leaf) {
        this()
        real = leaf.id
        word = leaf.word
        lemma = leaf.lemma
        tag = leaf.tag
        label = leaf.label
        parentLabel = leaf.parent.label
    }

}
