package com.frankfenton.deeperthinkserver

import com.frankfenton.deeperthinkserver.models.Label
import com.frankfenton.deeperthinkserver.models.Leaf
import com.frankfenton.deeperthinkserver.models.PseudoLeaf
import com.frankfenton.deeperthinkserver.models.Tag
import com.mongodb.BasicDBObject
import groovyx.gpars.GParsPool
import org.mongodb.morphia.aggregation.AggregationPipelineImpl
import org.mongodb.morphia.query.Query

/**
 * Created by EchelonFour on 24/02/2017.
 */
class Randos {
    private static final store = MongoStore.store
    private static final rand = new Random()

    private static Leaf sample(Query<Leaf> query) {
        AggregationPipelineImpl aggr = store.createAggregation(Leaf) as AggregationPipelineImpl
        aggr.match(query)
        aggr.stages.add(new BasicDBObject('$sample', new BasicDBObject("size", 1)))
        return aggr.aggregate(Leaf)[0]
    }

    static Leaf byLabel(PseudoLeaf l) {
        return sample(store.createQuery(Leaf).field('label').equal(l.label))
    }

    static Leaf byTag(PseudoLeaf l) {
        return sample(store.createQuery(Leaf).field('tag').equal(l.tag))
    }

    static Leaf byLabelAndTag(PseudoLeaf l) {
        return sample(store.createQuery(Leaf).field('label').equal(l.label).field('tag').equal(l.tag))
    }

    static Leaf byLemma(PseudoLeaf l) {
        return sample(store.createQuery(Leaf).field('lemma').equal(l.lemma))
    }
    static Leaf byReal(PseudoLeaf l) {
        return store.get(Leaf, l.real)
    }

    static int getRandomTreeSize() {
        return rand.nextInt(3) + 1
    }

    static Leaf somethingLikeThis(Leaf leaf) {
        if (leaf.tag == Tag.PUNCT || leaf.label == Label.METAROOT || leaf.label == Label.ROOT) {
            return leaf
        }
        return somethingLikeThis(new PseudoLeaf(leaf))
    }

    static Leaf somethingLikeThis(PseudoLeaf leaf) {
        if (leaf.label == Label.METAROOT) {
            return byLabel(leaf)
        }
        if (leaf.treeSize == 1) {
            //I can guess
            return new Leaf(leaf)
        }
        def thisRandomTreeSize = randomTreeSize
        if (leaf.tag != Tag.PUNCT && leaf.parentLabel == Label.ROOT && leaf.treeSize < thisRandomTreeSize && rand.nextInt(2) == 0) {
            //if the sentence is too short then shuffling does nothing. This must force it.
            return byLabelAndTag(leaf)
        }
        if (leaf.tag == Tag.PUNCT || leaf.label == Label.ROOT || leaf.treeSize < thisRandomTreeSize) {
            return byReal(leaf)
        }

        switch (rand.nextInt(5)) {
            case 0: return byLabel(leaf)
            case 1: return byTag(leaf)
            case 2: return byLabelAndTag(leaf)
            case 3: return byLemma(leaf)
            case 4: return byReal(leaf)
        }
    }
    static Leaf getRandoTree() {
        return GParsPool.withPool {
            return GParsPool.runForkJoin(new PseudoLeaf(label: Label.METAROOT)) { PseudoLeaf currentLeaf ->
//                println "Thread ${Thread.currentThread().name[-1]}: Getting $currentLeaf"
                Leaf realLeaf = somethingLikeThis(currentLeaf)
                for (leaf in realLeaf.pseudoLefts) {
                    forkOffChild(leaf)
                }
                for (leaf in realLeaf.pseudoRights) {
                    forkOffChild(leaf)
                }

                if (childrenResults) {
                    realLeaf.lefts = childrenResults.take(realLeaf.pseudoLefts.size())
                    realLeaf.rights = childrenResults.drop(realLeaf.pseudoLefts.size())
                }
                return realLeaf
            }

        }
    }
}

