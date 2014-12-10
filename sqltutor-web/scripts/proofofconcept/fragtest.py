#!/usr/bin/env python

class Frag(object):
    settype = set
    def __init__(self, sources, labels=None):
        settype = type(self).settype
        if not isinstance(sources, settype): sources = settype(sources)
        self.sources = sources
        if labels is None: labels = []
        if isinstance(labels, settype):
            self.labels = labels
        else:
            self.labels = settype(labels)
        
    def excludes(self, frag):
        for s in self.sources:
            if s in frag.sources:
                return True
        return False
        
    def frozen(self):
        if isinstance(self, FrozenFrag):
            return self
        return FrozenFrag(self.sources, self.labels)

    def merge(self, other):
        sources = self.sources.union(other.sources)
        labels = self.labels.union(other.labels)
        return Frag(sources=sources, labels=labels)
        
    def __str__(self):
        return '<{}, {}>'.format(self.sources, self.labels)
    
    def __repr__(self):
        return str(self)
        
class FrozenFrag(Frag):
    settype = frozenset
    def __hash__(self):
        p = 31
        h = 0
        h += p*hash(self.sources)
        h += p*hash(self.labels)
        return h
    def __eq__(self, other):
        return self.sources == other.sources and self.labels == other.labels

def pairwise_merge(frags, mustcover=None):
    frags = frags[:]
    i = 0
    while i < len(frags):
        first = frags[i]
        j = i + 1
        while j < len(frags):
            second = frags[j]
            if not first.excludes(second):
                new = first.merge(second)
                frags.append(new)
                print 'Merge {} x {} => {}'.format(first, second, new)
            j += 1
        i += 1
        
    if mustcover is None:
        mustcover = set()
        map(mustcover.update, (f.sources for f in frags))
        
    results = set(frag.frozen() for frag in frags if mustcover.issubset(frag.sources))
    print "Set results:\n"
    print '\n'.join( map(str, results) )
    return results
    

def merge_next(cmap):
    #frags = cmap.keys()
    #while frags:
    while cmap:
        #fragA = frags.peek()
        fragA, compatA = cmap.iteritems().next()
        if not compatA:
            del cmap[fragA]
            yield fragA # final node
            continue
        
        fragB = iter(compatA).next()
        compatB = cmap[fragB]
        
        print "Merging {} and {}".format(fragA, fragB)
        
        # merge fragments
        fragAB = fragA.merge(fragB)
        compatA.discard(fragB)
        compatB.discard(fragA)
        compatAB = compatA.intersection(compatB)
        cmap[fragAB] = compatAB
        
        print "New node:", fragAB
        print "Compat with:", compatAB
        
        if not compatA:
            del cmap[fragA]
        if not compatB:
            del cmap[fragB]
    print "Done, cmap:", cmap

f1 = Frag(["a1"], ["a1"])
f2 = Frag(["a2"], ["a2"])
f3 = Frag(["a3"], ["a3"])
f4 = Frag(["a1","a2"], ['a12'])
f5 = Frag(['a2', 'a3'], ['a23'])

frags = [f1,f2,f3,f4,f5]


#cmap = dict((f,set()) for f in frags)

#nfrags = len(frags)
#for i in xrange(0, nfrags):
    #a = frags[i]
    #for j in xrange(i+1, nfrags):
        #b = frags[j]
        #if not a.excludes(b):
            #cmap[a].add(b)
            #cmap[b].add(a)

#for node in merge_next(cmap):
    #print "Final node:", node

pairwise_merge(frags)
