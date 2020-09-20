function anchor=getanchor(nodes, m)
% function anchor=getanchor(nodes, m)
% pick anchors along the edges of the network
% Input
%    -- nodes: the (x,y) coordinates of the nodes
%    -- m: number of anchors per side, there are 4m anchors in total
% Output
%    -- anchor: the picked anchor, the first column is the node id, the
%    following columns are the x and y corrdinates for the anchor

anchor=zeros(1);
n=size(nodes,1);
mx=ceil(max(nodes(:,1)));
my=ceil(max(nodes(:,2)));
count=1; %total number of anchors

%pick anchors on strips along the x axes

for low=0:mx/m:mx-mx/m
    high=low+mx/m;
    nodeids=find( (nodes(:,1)<=high) & (nodes(:,1)>=low) );
    if(size(nodeids)>0)
        nodestrip=nodes(nodeids,2);
        [~,id2]=max(nodestrip);
        anchor(count)=nodeids(id2);
        count=count+1;
    
        [~,id2]=min(nodestrip);
        anchor(count)=nodeids(id2);
        count=count+1;
    end
end

%pick anchors on strips along the y axes

for low=0:mx/m:mx-mx/m
    high=low+mx/m;
    nodeids=find( (nodes(:,2)<=high)& (nodes(:,2)>=low));
    if(size(nodeids)>0)
        nodestrip=nodes(nodeids,1);
        [~,id2]=max(nodestrip);
        anchor(count)=nodeids(id2);
        count=count+1;
    
        [~,id2]=min(nodestrip);
        anchor(count)=nodeids(id2);
        count=count+1;
    end
end


%Optinally pick anchors in random
%anchor=rand(1,4*m);
%anchor= ceil(anchor.*n);

anchor=unique(anchor);
anchor=[anchor' nodes(anchor,:)];
end