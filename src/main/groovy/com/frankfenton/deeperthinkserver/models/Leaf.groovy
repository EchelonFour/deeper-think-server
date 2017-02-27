package com.frankfenton.deeperthinkserver.models

import org.bson.types.ObjectId
import org.mongodb.morphia.annotations.Entity
import org.mongodb.morphia.annotations.Field
import org.mongodb.morphia.annotations.Id
import org.mongodb.morphia.annotations.Index
import org.mongodb.morphia.annotations.Indexed
import org.mongodb.morphia.annotations.Indexes
import org.mongodb.morphia.annotations.Reference


/**
 * Created by EchelonFour on 23/02/2017.
 */
@Entity("leaf")
@Indexes(
        @Index(value = "labelTag", fields = [@Field("label"), @Field("tag")])
)
class Leaf {
    @Id
    ObjectId id = new ObjectId()

    String word
    @Indexed
    String lemma

    @Indexed
    Tag tag
    @Indexed
    Label label

    PseudoLeaf parent
    transient Leaf realParent

    int treeSize

    List<PseudoLeaf> pseudoLefts = new ArrayList<>()
    List<PseudoLeaf> pseudoRights = new ArrayList<>()

    transient List<Leaf> lefts = new ArrayList<>()

    transient List<Leaf> rights = new ArrayList<>()

    public Leaf() {

    }
    public Leaf(PseudoLeaf leaf) {
        this()
        this.word = leaf.word
        this.lemma = leaf.lemma
        this.tag = leaf.tag
        this.label = leaf.label
        this.treeSize = leaf.treeSize
    }

    public addToLeft(Leaf leaf) {
        leaf.realParent = this
        leaf.parent = new PseudoLeaf(this)
        this.lefts.add(leaf)
        this.pseudoLefts.add(new PseudoLeaf(leaf))
    }

    public addToRight(Leaf leaf) {
        leaf.realParent = this
        leaf.parent = new PseudoLeaf(this)
        this.rights.add(leaf)
        this.pseudoRights.add(new PseudoLeaf(leaf))
    }

    public int getTreeSize() {
        if (!this.treeSize) {
            this.calculateTreeSize()
        }
        return this.treeSize
    }
    public int calculateTreeSize() {
        int childrenTotal = 0
        this.lefts.eachWithIndex{ Leaf leaf, int i ->
            int leafTotal = leaf.calculateTreeSize()
            this.pseudoLefts[i].treeSize = leafTotal
            childrenTotal += leafTotal
        }
        this.rights.eachWithIndex{ Leaf leaf, int i ->
            int leafTotal = leaf.calculateTreeSize()
            this.pseudoRights[i].treeSize = leafTotal
            childrenTotal += leafTotal
        }
        this.treeSize = 1 + childrenTotal
        return this.treeSize
    }

    String toSentence() {
        def sentence = ''
        def words = new LinkedList<Leaf>()
        this.lnrSearch(words)
        Leaf previousLeaf = null
        for (leaf in words) {
            if (!leaf.word) {
                continue
            }
            String word = leaf.word
            if (word == 'i') {
                word = 'I'
            } else if (word == 'n\'t') {
                word = 'not'
            } else if (word == '\'m') {
                word = 'am'
            }
            if (previousLeaf == null || previousLeaf.word in ['.', '?', '!']) {
                word = word.capitalize()
            }
            if (leaf.tag != Tag.PUNCT && !word.startsWith("'") && previousLeaf != null) {
                sentence <<= ' '
            }
            sentence <<= word
            previousLeaf = leaf
        }
        return sentence
    }

    void lnrSearch(LinkedList<Leaf> sentence) {
        for (leaf in this.lefts) {
            leaf.lnrSearch(sentence)
        }
        sentence.add(this)
        for (leaf in this.rights) {
            leaf.lnrSearch(sentence)
        }
    }

}
