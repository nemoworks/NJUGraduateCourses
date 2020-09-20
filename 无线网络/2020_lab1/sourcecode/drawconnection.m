function drawconnection(nodes,edges,anchor)
% Function drawconnection(nodes,edges)
% Draw the location of nodes and edges between the nodes
% Input:
%  -- nodes: the (x,y) coordinates of the nodes
%  -- edges: the edgelist
%  -- anchor: the position of anchor nodes

if(nargin==2)
    anchor=[];
end

n=size(nodes,1);
m=size(edges,1);

clf;
hold on;
% draw all the edges with gray lines
for j=1:m
    if(edges(j,1)>edges(j,2)) % only draw half of the edges 
        h= line([nodes(edges(j,1),1) nodes(edges(j,2),1)], [nodes(edges(j,1),2) nodes(edges(j,2),2)]);
        set(h,'Color',[0.4 0.4 0.4]);
    end
end
% draw nodes with small green circles
scatter(nodes(:,1),nodes(:,2),49,'g','filled');
% draw anchors with small red circles
if(size(anchor,1)>0)
    scatter(anchor(:,2),anchor(:,3),64,'r','filled');
end
xlabel('X');
ylabel('Y');
axis( [floor(min(nodes(:,1)))  ceil(max(nodes(:,1))) floor(min(nodes(:,2))) ceil(max(nodes(:,2))) ] );
hold off;
end